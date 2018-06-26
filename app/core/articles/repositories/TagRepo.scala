package core.articles.repositories

import commons.models.{IdMetaModel, Property}
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import core.articles.models.{Tag, TagId, TagMetaModel}
import slick.dbio.{DBIO, Effect}
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class TagRepo(implicit private val ec: ExecutionContext)
  extends BaseRepo[TagId, Tag, TagTable] {

  def findByNames(tagNames: Seq[String]): DBIO[Seq[Tag]] = {
    if (tagNames == null || tagNames.isEmpty) DBIO.successful(Seq.empty)
    else query
      .filter(_.name.inSet(tagNames))
      .result
  }

  override protected val mappingConstructor: slick.lifted.Tag => TagTable = new TagTable(_)

  override protected val modelIdMapping: BaseColumnType[TagId] = TagId.tagIdDbMapping

  override protected val metaModel: IdMetaModel = TagMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], TagTable => Rep[_]] = Map(
    TagMetaModel.id -> (table => table.id),
    TagMetaModel.name -> (table => table.name),
  )

}

protected class TagTable(tableTag: slick.lifted.Tag) extends IdTable[TagId, Tag](tableTag, "tags")
  with JavaTimeDbMappings {

  def name: Rep[String] = column(TagMetaModel.name.name)

  def * : ProvenShape[Tag] = (id, name) <> ((Tag.apply _).tupled, Tag.unapply)
}
