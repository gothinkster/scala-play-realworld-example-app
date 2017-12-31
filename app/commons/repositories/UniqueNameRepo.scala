package commons.repositories

import commons.models.WithName
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted._

trait UniqueNameRepo[ModelId <: BaseId[Long],
WithNameModel <: WithId[Long, ModelId] with WithName,
ModelTable <: IdTable[ModelId, WithNameModel] with WithNameBaseTable]
  extends BaseRepo[ModelId, WithNameModel, ModelTable] {

  def byName(name: String): DBIO[Option[WithNameModel]] = {
    query
      .filter(_.name === name)
      .result
      .headOption
  }

}

trait WithNameBaseTable {
  self: Table[_] =>

  def name: Rep[String] = column("name")
}

