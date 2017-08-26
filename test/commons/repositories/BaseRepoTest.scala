//package commons.repositories
//
//import java.time.LocalDateTime
//
//import com.softwaremill.macwire.wire
//import commons.models._
//import commons.repositories.mappings.JavaTimeDbMappings
//import slick.lifted.{ProvenShape, Rep, Tag}
//import testhelpers.{FixedDateTimeProvider, PlayWithFoodWithServerBaseTest, TestUtils}
//
//class BaseRepoTest extends PlayWithFoodWithServerBaseTest {
//
//  class TestModelComponents extends PlayWithFoodWithTestConfig {
//    override lazy val dateTimeProvider = new FixedDateTimeProvider(LocalDateTime.now())
//
//    lazy val testModelRepo: TestModelRepo = wire[TestModelRepo]
//  }
//
//  override def components: TestModelComponents = new TestModelComponents
//
//  "base repo" should {
//    "sort by id desc by deafult" in {
//      implicit val c = components
//      import c._
//
//      // given
//
//      val repo = testModelRepo
//
//      val apple = runAndAwaitResult(repo.create(NewTestModel("apple", 21).toTestModel))
//      val orange = runAndAwaitResult(repo.create(NewTestModel("orange", 12).toTestModel))
//      val peach = runAndAwaitResult(repo.create(NewTestModel("peach", 17).toTestModel))
//
//      // when
//      val all = repo.all(List())
//
//      // then
//      runAndAwaitResult(all) match {
//        case Seq(elem1, elem2, elem3) =>
//          elem1.mustBe(peach)
//          elem2.mustBe(orange)
//          elem3.mustBe(apple)
//        case _ => fail()
//      }
//
//    }
//
//    "sort by age desc and id asc" in {
//      implicit val c = components
//      import c._
//
//      // given
//
//      val repo = testModelRepo
//
//      val apple = runAndAwaitResult(repo.create(TestModel(TestModelId(-1), "apple", 1, null, null)))
//      val orange = runAndAwaitResult(repo.create(TestModel(TestModelId(-1), "orange", 5, null, null)))
//      val peach = runAndAwaitResult(repo.create(TestModel(TestModelId(-1), "peach", 5, null, null)))
//
//      // when
//      val all = repo.all(List(Ordering(TestModelMetaModel.age, Descending), Ordering(TestModelMetaModel.id, Ascending)))
//
//      // then
//      val result = runAndAwaitResult(all)
//      result match {
//        case Seq(elem1, elem2, elem3) =>
//          elem1.mustBe(orange)
//          elem2.mustBe(peach)
//          elem3.mustBe(apple)
//        case _ => fail()
//      }
//    }
//
//    "set modified at date time when updated" in {
//      implicit val c = components
//      import c._
//
//      // given
//      val createDateTimeProvider = new FixedDateTimeProvider(LocalDateTime.now)
//      val createRepo = new TestModelRepo(createDateTimeProvider)
//
//      val updatedAtDateTime = LocalDateTime.now
//
//      val apple = runAndAwaitResult(createRepo.create(TestModel(TestModelId(-1), "apple", 1, null, null)))
//      val expectedApple = apple.copy(modifiedAt = updatedAtDateTime)
//
//      val updateDateTimeProvider = new FixedDateTimeProvider(updatedAtDateTime)
//      val updateRepo = new TestModelRepo(updateDateTimeProvider)
//
//      // when
//
//      val updateAction = updateRepo.update(expectedApple)
//
//      // then
//      val result = runAndAwaitResult(updateAction)
//
//      result.mustBe(expectedApple)
//    }
//  }
//
//  override protected def beforeEach(): Unit = {
//    val c = components
//    runServerAndExecute(c.application) {
//      val testModelRepo = c.testModelRepo
//      TestUtils.runAndAwaitResult(testModelRepo.createTable)(c.actionRunner, duration)
//    }
//  }
//
//  override protected def afterEach(): Unit = {
//    super.afterEach()
//
//    val c = components
//    runServerAndExecute(c.application) {
//      val testModelRepo = c.testModelRepo
//      TestUtils.runAndAwaitResult(testModelRepo.dropTable)(c.actionRunner, duration)
//    }
//  }
//
//}
//
// case class TestModel(id: TestModelId,
//                      override val name: String,
//                      age: Int,
//                      override val createdAt: LocalDateTime,
//                      override val modifiedAt: LocalDateTime
//                     )
//   extends WithId[Long, TestModelId]
//     with WithDateTimes[TestModel]
//     with WithName
//      {
//
//    override def updateCreatedAt(dateTime: LocalDateTime): TestModel = copy(createdAt = dateTime)
//
//    override def updateModifiedAt(dateTime: LocalDateTime): TestModel = copy(modifiedAt = dateTime)
//  }
//
//  case class NewTestModel(name: String, age: Int) {
//    def toTestModel: TestModel = TestModel(TestModelId(-1), name, age, null, null)
//  }
//
//  object TestModelMetaModel extends IdMetaModel {
//    override type ModelId = TestModelId
//
//    val name: Property[String] = Property("name")
//    val age: Property[Int] = Property("age")
//  }
//
//  import slick.dbio.DBIO
//  import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, TableQuery => _, Rep => _, _}
//  import slick.lifted._
//
//class TestModelRepo(
//                     override protected val dateTimeProvider: DateTimeProvider)
//  extends BaseRepo[TestModelId, TestModel, TestModelTable]
//    with AuditDateTimeRepo[TestModelId, TestModel, TestModelTable]
//    with UniqueNameRepo[TestModelId, TestModel, TestModelTable]
//    with JavaTimeDbMappings {
//
//    def createTable: DBIO[Int] =
//      sqlu"""
//      CREATE TABLE `test_model` (
//        `id` INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
//        `name` VARCHAR(255) NOT NULL,
//        `age` INT NOT NULL,
//        `created_at` DATETIME,
//        `modified_at` DATETIME
//      );
//    """
//
//    def dropTable: DBIO[Int] =
//      sqlu"""
//      DROP TABLE `test_model`;
//    """
//
//    override protected val mappingConstructor: Tag => TestModelTable = new TestModelTable(_)
//
//    override protected val modelIdMapping: BaseColumnType[TestModelId] = MappedColumnType.base[TestModelId, Long](
//      vo => vo.value,
//      id => TestModelId(id)
//    )
//
//    override protected val metaModel: IdMetaModel = TestModelMetaModel
//
//    override protected val metaModelToColumnsMapping: Map[Property[_], (TestModelTable) => Rep[_]] = Map(
//      TestModelMetaModel.id -> (table => table.id),
//      TestModelMetaModel.name -> (table => table.name),
//      TestModelMetaModel.age -> (table => table.age)
//    )
//  }
//
//
//class TestModelTable(tag: Tag) extends IdTable[TestModelId, TestModel](tag, "test_model")
//  with WithNameBaseTable
//  with AuditDateTimeTable
//  with JavaTimeDbMappings {
//
//  def age: Rep[Int] = column(TestModelMetaModel.age.name)
//
//  def * : ProvenShape[TestModel] = (id, name, age, createdAt, modifiedAt) <> (TestModel.tupled, TestModel.unapply)
//}
//
//case class TestModelId(override val id: Long) extends AnyVal with BaseId[Long]
//
