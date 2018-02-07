package core.articles.repositories

import commons.models.{IdMetaModel, Property}
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import core.articles.models.{Tag, TagId, TagMetaModel}
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class TagRepo(implicit private val ec: ExecutionContext)
  extends BaseRepo[TagId, Tag, TagTable] {

  def createIfNotExist(tags: Seq[Tag]): DBIO[Seq[Tag]] = {
    if (tags == null || tags.isEmpty) DBIO.successful(Seq.empty)
    else {
      fetchExistingTags(tags).flatMap(existingTags => {
        createNewTags(tags, existingTags)
          .map(newTags => newTags ++ existingTags)
      })
    }
  }

  private def createNewTags(givenTags: Seq[Tag], existingTags: Seq[Tag]) = {
    val newTags = getNewTags(givenTags, existingTags)
    insertAndGet(newTags)
  }

  private def getNewTags(givenTags: Seq[Tag], existingTags: Seq[Tag]) = {
    val tagNames = givenTags.map(_.name).toSet
    val existingTagNames = existingTags.map(_.name).toSet
    val newTagValues = tagNames.diff(existingTagNames)
    newTagValues.map(Tag.from)
  }

  private def fetchExistingTags(tags: Seq[Tag]) = {
    query
      .filter(table => {
        val ids = tags.map(_.id)
        val names = tags.map(_.name)

        table.id.inSet(ids) || table.name.inSet(names)
      })
      .result
  }

  override protected val mappingConstructor: slick.lifted.Tag => TagTable = new TagTable(_)

  override protected val modelIdMapping: BaseColumnType[TagId] = TagId.tagIdDbMapping

  override protected val metaModel: IdMetaModel = TagMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (TagTable) => Rep[_]] = Map(
    TagMetaModel.id -> (table => table.id),
    TagMetaModel.name -> (table => table.name),
  )

}

protected class TagTable(tableTag: slick.lifted.Tag) extends IdTable[TagId, Tag](tableTag, "tags")
  with JavaTimeDbMappings {

  def name: Rep[String] = column(TagMetaModel.name.name)

  def * : ProvenShape[Tag] = (id, name) <> ((Tag.apply _).tupled, Tag.unapply)
}
