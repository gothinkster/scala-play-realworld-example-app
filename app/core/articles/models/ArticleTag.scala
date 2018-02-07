
package core.articles.models

import commons.models.{BaseId, IdMetaModel, Property, WithId}
import play.api.libs.json._
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class ArticleTag(id: ArticleTagId,
                      articleId: ArticleId,
                      tagId: TagId) extends WithId[Long, ArticleTagId]

object ArticleTag {
  def from(article: Article, tag: Tag): ArticleTag = ArticleTag(ArticleTagId(-1), article.id, tag.id)
}

case class ArticleTagId(override val value: Long) extends AnyVal with BaseId[Long]

object ArticleTagId {
  implicit val articleTagIdFormat: Format[ArticleTagId] = new Format[ArticleTagId] {
    override def reads(json: JsValue): JsResult[ArticleTagId] =
      Reads.LongReads.reads(json).map(ArticleTagId(_))

    override def writes(o: ArticleTagId): JsNumber = Writes.LongWrites.writes(o.value)
  }

  implicit val articleTagIdDbMapping: BaseColumnType[ArticleTagId] = MappedColumnType.base[ArticleTagId, Long](
    vo => vo.value,
    id => ArticleTagId(id)
  )
}

object ArticleTagMetaModel extends IdMetaModel {

  val articleId: Property[ArticleId] = Property("article_id")
  val tagId: Property[TagId] = Property("tag_id")

  override type ModelId = ArticleTagId
}