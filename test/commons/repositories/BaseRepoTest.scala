package commons.repositories

import java.time.Instant

import commons.models._
import commons.repositories.mappings.JavaTimeDbMappings
import commons.services.ActionRunner
import commons_test.test_helpers.RealWorldWithServerAndTestConfigBaseTest.RealWorldWithTestConfig
import commons_test.test_helpers.{ProgrammaticDateTimeProvider, RealWorldWithServerBaseTest, TestUtils}
import play.api.ApplicationLoader.Context
import slick.lifted.{ProvenShape, Rep, Tag}

class BaseRepoTest extends RealWorldWithServerBaseTest {

  val dateTime: Instant = Instant.now
  val programmaticDateTimeProvider: ProgrammaticDateTimeProvider = new ProgrammaticDateTimeProvider

  override type TestComponents = AppWithTestRepo

  override def createComponents: TestComponents = {
    new AppWithTestRepo(programmaticDateTimeProvider, context)
  }

  "Base repo" should "sort by id desc by default" in runAndAwait {
    val testModelRepo = components.testModelRepo

    val apple = testModelRepo.createBlocking(NewTestModel("apple", 21).toTestModel(dateTime))
    val orange = testModelRepo.createBlocking(NewTestModel("orange", 12).toTestModel(dateTime))
    val peach = testModelRepo.createBlocking(NewTestModel("peach", 17).toTestModel(dateTime))

    // when
    val all = testModelRepo.findAll(List())

    // then
    all.map({
      case Seq(elem1, elem2, elem3) =>
        elem1.mustBe(peach)
        elem2.mustBe(orange)
        elem3.mustBe(apple)
      case _ => fail()
    })
  }(components.actionRunner)

  it should "sort by age desc and id asc" in runAndAwait {
    val testModelRepo = components.testModelRepo
    // given
    val apple = testModelRepo.createBlocking(TestModel(TestModelId(-1), "apple", 1, dateTime, dateTime))
    val orange = testModelRepo.createBlocking(TestModel(TestModelId(-1), "orange", 5, dateTime, dateTime))
    val peach = testModelRepo.createBlocking(TestModel(TestModelId(-1), "peach", 5, dateTime, dateTime))

    // when
    val all = testModelRepo.findAll(List(Ordering(TestModelMetaModel.age, Descending),
      Ordering(TestModelMetaModel.id, Ascending)))

    // then
    all.map({
      case Seq(elem1, elem2, elem3) =>
        elem1.mustBe(orange)
        elem2.mustBe(peach)
        elem3.mustBe(apple)
      case _ => fail()
    })
  }(components.actionRunner)

  it should "set modified at date time when updated" in runAndAwait {
    val testModelRepo = components.testModelRepo
    // given
    programmaticDateTimeProvider.currentTime = dateTime
    val apple = testModelRepo.createBlocking(TestModel(TestModelId(-1), "apple", 1, dateTime, dateTime))

    val laterDateTime = Instant.now
    programmaticDateTimeProvider.currentTime = laterDateTime

    val updatedApple = apple.copy(updatedAt = laterDateTime)

    // when
    val updateAction = testModelRepo.updateAndGet(updatedApple)

    // then
    updateAction.map(result => {
      result.createdAt.mustBe(dateTime)
      result.updatedAt.mustBe(laterDateTime)
    })
  }(components.actionRunner)

  override def afterServerStarted(): Unit = {
    runAndAwait(components.testModelRepo.createTable)(components.actionRunner)
  }
}

class AppWithTestRepo(dateTimeProvider: ProgrammaticDateTimeProvider, context: Context) extends RealWorldWithTestConfig(context) {

  lazy val testModelRepo: TestModelRepo = new TestModelRepo(actionRunner)
}


case class TestModel(id: TestModelId,
                     name: String,
                     age: Int,
                     createdAt: Instant,
                     updatedAt: Instant) extends WithId[Long, TestModelId]

case class NewTestModel(name: String, age: Int) {
  def toTestModel(now: Instant): TestModel = TestModel(TestModelId(-1), name, age, now, now)
}

object TestModelMetaModel extends IdMetaModel {
  override type ModelId = TestModelId

  val name: Property[String] = Property("name")
  val age: Property[Int] = Property("age")
}

import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

class TestModelRepo(private val actionRunner: ActionRunner)
  extends BaseRepo[TestModelId, TestModel, TestModelTable]
    with JavaTimeDbMappings {

  override protected val mappingConstructor: Tag => TestModelTable = new TestModelTable(_)
  override protected val modelIdMapping: BaseColumnType[TestModelId] = TestModelId.testModelIdDbMapping
  override protected val metaModel: IdMetaModel = TestModelMetaModel
  override protected val metaModelToColumnsMapping: Map[Property[_], TestModelTable => Rep[_]] = Map(
    TestModelMetaModel.id -> (table => table.id),
    TestModelMetaModel.name -> (table => table.name),
    TestModelMetaModel.age -> (table => table.age)
  )

  def createBlocking(testModel: TestModel): TestModel = {
    val action = insertAndGet(testModel)
    TestUtils.runAndAwaitResult(action)(actionRunner)
  }

  def createTable: DBIO[Int] =
    sqlu"""
      CREATE TABLE test_model (
        id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(255) NOT NULL,
        age INT NOT NULL,
        created_at TIMESTAMP WITH TIME ZONE,
        updated_at TIMESTAMP WITH TIME ZONE
      );
    """

  def dropTable: DBIO[Int] =
    sqlu"""
      DROP TABLE test_model;
    """
}


class TestModelTable(tag: Tag) extends IdTable[TestModelId, TestModel](tag, "test_model")
  with JavaTimeDbMappings {

  def * : ProvenShape[TestModel] = (id, name, age, createdAt, updatedAt) <> (TestModel.tupled, TestModel.unapply)

  def age: Rep[Int] = column("age")

  def name: Rep[String] = column("name")

  def createdAt: Rep[Instant] = column("created_at")

  def updatedAt: Rep[Instant] = column("updated_at")
}

case class TestModelId(override val value: Long) extends AnyVal with BaseId[Long]

object TestModelId {
  implicit val testModelIdDbMapping: BaseColumnType[TestModelId] = MappedColumnType.base[TestModelId, Long](
    vo => vo.value,
    id => TestModelId(id)
  )
}

