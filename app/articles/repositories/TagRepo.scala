package articles.repositories

import articles.models.{Tag, _}
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class TagRepo(implicit private val ec: ExecutionContext) {
  import TagTable.tags

  def findByNames(tagNames: Seq[String]): DBIO[Seq[Tag]] = {
    if (tagNames == null || tagNames.isEmpty) DBIO.successful(Seq.empty)
    else tags
      .filter(_.name.inSet(tagNames))
      .result
  }

  def insertAndGet(models: Iterable[Tag]): DBIO[Seq[Tag]] = {
    if (models == null && models.isEmpty) DBIO.successful(Seq.empty)
    else tags.returning(tags.map(_.id))
      .++=(models)
      .flatMap(ids => findByIds(ids))
  }

  private def findByIds(modelIds: Iterable[TagId]): DBIO[Seq[Tag]] = {
    if (modelIds == null || modelIds.isEmpty) DBIO.successful(Seq.empty)
    else tags
      .filter(_.id inSet modelIds)
      .result
  }

  def findAll(): DBIO[Seq[Tag]] = {
      tags
        .sortBy(_.id.desc)
        .result
  }

}

object TagTable {
  val tags = TableQuery[Tags]

  protected class Tags(tableTag: slick.lifted.Tag) extends Table[Tag](tableTag, "tags") {

    def id: Rep[TagId] = column[TagId]("id", O.PrimaryKey, O.AutoInc)

    def name: Rep[String] = column(TagMetaModel.name.name)

    def * : ProvenShape[Tag] = (id, name) <> ((Tag.apply _).tupled, Tag.unapply)
  }

}
