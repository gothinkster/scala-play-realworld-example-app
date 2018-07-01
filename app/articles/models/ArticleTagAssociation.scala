
package articles.models

import commons.models.{BaseId, IdMetaModel, Property, WithId}
import play.api.libs.json._
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class ArticleTagAssociation(id: ArticleTagAssociationId,
                                 articleId: ArticleId,
                                 tagId: TagId) extends WithId[Long, ArticleTagAssociationId]

object ArticleTagAssociation {
  def from(article: Article, tag: Tag): ArticleTagAssociation =
    ArticleTagAssociation(ArticleTagAssociationId(-1), article.id, tag.id)
}

case class ArticleTagAssociationId(override val value: Long) extends AnyVal with BaseId[Long]

object ArticleTagAssociationId {
  implicit val articleTagAssociationIdFormat: Format[ArticleTagAssociationId] = new Format[ArticleTagAssociationId] {
    override def reads(json: JsValue): JsResult[ArticleTagAssociationId] =
      Reads.LongReads.reads(json).map(ArticleTagAssociationId(_))

    override def writes(o: ArticleTagAssociationId): JsNumber = Writes.LongWrites.writes(o.value)
  }

  implicit val articleTagAssociationIdDbMapping: BaseColumnType[ArticleTagAssociationId] =
    MappedColumnType.base[ArticleTagAssociationId, Long](
      vo => vo.value,
      id => ArticleTagAssociationId(id)
    )
}

object ArticleTagAssociationMetaModel extends IdMetaModel {

  override type ModelId = ArticleTagAssociationId
  val articleId: Property[ArticleId] = Property("article_id")
  val tagId: Property[TagId] = Property("tag_id")
}