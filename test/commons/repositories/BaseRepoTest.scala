package commons.repositories

import java.time.Instant

import commons.models._
import commons.repositories.mappings.JavaTimeDbMappings
import slick.lifted.{ProvenShape, Rep, Tag}
import testhelpers.{ProgrammaticDateTimeProvider, RealWorldWithServerBaseTest, TestUtils}

class BaseRepoTest extends RealWorldWithServerBaseTest {

  val dateTime: Instant = Instant.now
  val programmaticDateTimeProvider: ProgrammaticDateTimeProvider = new ProgrammaticDateTimeProvider

  class AppWithTestRepo extends RealWorldWithTestConfig {

    override lazy val dateTimeProvider: ProgrammaticDateTimeProvider = programmaticDateTimeProvider

    lazy val testModelRepo: TestModelRepo = new TestModelRepo(dateTimeProvider, actionRunner)
  }

  override implicit def components: AppWithTestRepo = new AppWithTestRepo

  def testModelRepo(implicit components: AppWithTestRepo): TestModelRepo = components.testModelRepo

  "Base repo" should {
    "sort by id desc by deafult" in {
      val apple = testModelRepo.createBlocking(NewTestModel("apple", 21).toTestModel)
      val orange = testModelRepo.createBlocking(NewTestModel("orange", 12).toTestModel)
      val peach = testModelRepo.createBlocking(NewTestModel("peach", 17).toTestModel)

      // when
      val all = testModelRepo.all(List())

      // then
      runAndAwaitResult(all) match {
        case Seq(elem1, elem2, elem3) =>
          elem1.mustBe(peach)
          elem2.mustBe(orange)
          elem3.mustBe(apple)
        case _ => fail()
      }
    }

    "sort by age desc and id asc" in {
      // given
      val apple = testModelRepo.createBlocking(TestModel(TestModelId(-1), "apple", 1, null, null))
      val orange = testModelRepo.createBlocking(TestModel(TestModelId(-1), "orange", 5, null, null))
      val peach = testModelRepo.createBlocking(TestModel(TestModelId(-1), "peach", 5, null, null))

      // when
      val all = testModelRepo.all(List(Ordering(TestModelMetaModel.age, Descending),
        Ordering(TestModelMetaModel.id, Ascending)))

      // then
      runAndAwaitResult(all) match {
        case Seq(elem1, elem2, elem3) =>
          elem1.mustBe(orange)
          elem2.mustBe(peach)
          elem3.mustBe(apple)
        case _ => fail()
      }
    }

    "set modified at date time when updated" in {
      // given
      programmaticDateTimeProvider.currentTime = dateTime
      val apple = testModelRepo.createBlocking(TestModel(TestModelId(-1), "apple", 1, null, null))

      val laterDateTime = Instant.now
      programmaticDateTimeProvider.currentTime = laterDateTime

      // when
      val updateAction = testModelRepo.update(apple)

      // then
      val result = runAndAwaitResult(updateAction)

      result.createdAt.mustBe(dateTime)
      result.updatedAt.mustBe(laterDateTime)
    }
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    val testModelRepo = components.testModelRepo
    TestUtils.runAndAwaitResult(testModelRepo.createTable)(components.actionRunner, defaultAwaitDuration)
  }

  override protected def afterEach(): Unit = {
    super.afterEach()

    val testModelRepo = components.testModelRepo
    TestUtils.runAndAwaitResult(testModelRepo.dropTable)(components.actionRunner, defaultAwaitDuration)
  }

}

case class TestModel(id: TestModelId,
                     override val name: String,
                     age: Int,
                     override val createdAt: Instant,
                     override val updatedAt: Instant
                    )
  extends WithId[Long, TestModelId]
    with WithDateTimes[TestModel]
    with WithName {

  override def updateCreatedAt(dateTime: Instant): TestModel = copy(createdAt = dateTime)

  override def updateUpdatedAt(dateTime: Instant): TestModel = copy(updatedAt = dateTime)
}

case class NewTestModel(name: String, age: Int) {
  def toTestModel: TestModel = TestModel(TestModelId(-1), name, age, null, null)
}

object TestModelMetaModel extends IdMetaModel {
  override type ModelId = TestModelId

  val name: Property[String] = Property("name")
  val age: Property[Int] = Property("age")
}

import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

class TestModelRepo(override protected val dateTimeProvider: DateTimeProvider,
                    implicit private var actionRunner: ActionRunner)
  extends BaseRepo[TestModelId, TestModel, TestModelTable]
    with AuditDateTimeRepo[TestModelId, TestModel, TestModelTable]
    with UniqueNameRepo[TestModelId, TestModel, TestModelTable]
    with JavaTimeDbMappings {

  def createBlocking(testModel: TestModel): TestModel = {
    val action = create(testModel)
    TestUtils.runAndAwaitResult(action)
  }

  def createTable: DBIO[Int] =
    sqlu"""
      CREATE TABLE test_model (
        id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        age INT NOT NULL,
        created_at DATETIME,
        updated_at DATETIME
      );
    """

  def dropTable: DBIO[Int] =
    sqlu"""
      DROP TABLE test_model;
    """

  override protected val mappingConstructor: Tag => TestModelTable = new TestModelTable(_)

  override protected val modelIdMapping: BaseColumnType[TestModelId] = TestModelId.testModelIdDbMapping

  override protected val metaModel: IdMetaModel = TestModelMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (TestModelTable) => Rep[_]] = Map(
    TestModelMetaModel.id -> (table => table.id),
    TestModelMetaModel.name -> (table => table.name),
    TestModelMetaModel.age -> (table => table.age)
  )
}


class TestModelTable(tag: Tag) extends IdTable[TestModelId, TestModel](tag, "test_model")
  with WithNameBaseTable
  with AuditDateTimeTable
  with JavaTimeDbMappings {

  def age: Rep[Int] = column(TestModelMetaModel.age.name)

  def * : ProvenShape[TestModel] = (id, name, age, createdAt, updatedAt) <> (TestModel.tupled, TestModel.unapply)
}

case class TestModelId(override val value: Long) extends AnyVal with BaseId[Long]

object TestModelId {
  implicit val testModelIdDbMapping: BaseColumnType[TestModelId] = MappedColumnType.base[TestModelId, Long](
    vo => vo.value,
    id => TestModelId(id)
  )
}

