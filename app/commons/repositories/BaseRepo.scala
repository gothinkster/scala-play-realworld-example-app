package commons.repositories

import commons.exceptions.MissingModelException
import commons.models.{BaseId, Descending, IdMetaModel, Ordering, Property, WithId}
import commons.utils.DbioUtils
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted._

import scala.concurrent.ExecutionContext.Implicits._

trait BaseRepo[ModelId <: BaseId[Long], Model <: WithId[Long, ModelId], ModelTable <: IdTable[ModelId, Model]] {
  // lazy required to init table query with concrete mappingConstructor value
  lazy val query: TableQuery[ModelTable] = TableQuery[ModelTable](mappingConstructor)
  protected val mappingConstructor: Tag => ModelTable
  implicit protected val modelIdMapping: BaseColumnType[ModelId]
  protected val metaModelToColumnsMapping: Map[Property[_], ModelTable => Rep[_]]
  protected val metaModel: IdMetaModel

  def findAll: DBIO[Seq[Model]] = findAll(List(Ordering(metaModel.id, Descending)))

  def findAll(orderings: Seq[Ordering]): DBIO[Seq[Model]] = {
    if (orderings == null || orderings.isEmpty) findAll
    else {
      // multiple sortBy calls are reversed comparing to SQLs order by clause
      val slickOrderings = orderings.map(toSlickOrderingSupplier).reverse

      var sortQuery = query.sortBy(slickOrderings.head)
      slickOrderings.tail.foreach(getSlickOrdering => {
        sortQuery = sortQuery.sortBy(getSlickOrdering)
      })

      sortQuery.result
    }
  }

  def insertAndGet(model: Model): DBIO[Model] = {
    require(model != null)

    insertAndGet(Seq(model))
      .map(_.head)
  }

  def insertAndGet(models: Iterable[Model]): DBIO[Seq[Model]] = {
    if (models == null && models.isEmpty) DBIO.successful(Seq.empty)
    else query.returning(query.map(_.id))
      .++=(models)
      .flatMap(ids => findByIds(ids))
  }

  def findByIds(modelIds: Iterable[ModelId]): DBIO[Seq[Model]] = {
    if (modelIds == null || modelIds.isEmpty) DBIO.successful(Seq.empty)
    else query
      .filter(_.id inSet modelIds)
      .result
  }

  def findByIds(modelIds: Iterable[ModelId], ordering: Ordering): DBIO[Seq[Model]] = {
    if (modelIds == null || modelIds.isEmpty) DBIO.successful(Seq.empty)
    else query
      .filter(_.id inSet modelIds)
      .sortBy(toSlickOrderingSupplier(ordering))
      .result
  }

  protected def toSlickOrderingSupplier(ordering: Ordering): ModelTable => ColumnOrdered[_] = {
    implicit val Ordering(property, direction) = ordering
    val getColumn = metaModelToColumnsMapping(property)
    getColumn.andThen(RepoHelper.createSlickColumnOrdered)
  }

  def insert(model: Model): DBIO[ModelId] = {
    require(model != null)

    insert(Seq(model))
      .map(_.head)
  }

  def insert(models: Iterable[Model]): DBIO[Seq[ModelId]] = {
    if (models != null && models.isEmpty) DBIO.successful(Seq.empty)
    else query.returning(query.map(_.id)).++=(models)
  }

  def updateAndGet(model: Model): DBIO[Model] = {
    require(model != null)

    query
      .filter(_.id === model.id)
      .update(model)
      .flatMap(_ => findById(model.id))
  }

  def findByIdOption(modelId: ModelId): DBIO[Option[Model]] = {
    require(modelId != null)

    findByIds(Seq(modelId))
      .map(_.headOption)
  }

  def findById(modelId: ModelId): DBIO[Model] = {
    findByIdOption(modelId)
      .flatMap(maybeModel => DbioUtils.optionToDbio(maybeModel, new MissingModelException(s"model id: $modelId")))
  }

  def delete(modelId: ModelId): DBIO[Int] = {
    require(modelId != null)

    delete(Seq(modelId))
  }

  def delete(modelIds: Seq[ModelId]): DBIO[Int] = {
    if (modelIds == null || modelIds.isEmpty) DBIO.successful(0)
    else query
      .filter(_.id inSet modelIds)
      .delete
  }

}

abstract class IdTable[Id <: BaseId[Long], Entity <: WithId[Long, Id]]
(tag: Tag, schemaName: Option[String], tableName: String)
(implicit val mapping: BaseColumnType[Id])
  extends Table[Entity](tag, schemaName, tableName) {

  protected val idColumnName: String = "id"

  def this(tag: Tag, tableName: String)(implicit mapping: BaseColumnType[Id]) = this(tag, None, tableName)

  final def id: Rep[Id] = column[Id](idColumnName, O.PrimaryKey, O.AutoInc)
}