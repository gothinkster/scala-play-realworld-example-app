package core.articles.models

import java.time.Instant

import commons.models.{IdMetaModel, Property, WithDateTimes}
import commons.repositories.{BaseId, WithId}
import play.api.libs.json._

case class Article(id: ArticleId,
                   slug: String,
                   title: String,
                   description: String,
                   body: String,
                   override val createdAt: Instant,
                   override val updatedAt: Instant,
                  )
  extends WithId[Long, ArticleId]
    with WithDateTimes[Article] {

  override def updateCreatedAt(dateTime: Instant): Article = copy(createdAt = dateTime)

  override def updateUpdatedAt(dateTime: Instant): Article = copy(updatedAt = dateTime)

}

object Article {
  implicit val articleFormat: Format[Article] = Json.format[Article]
}

case class ArticleId(override val id: Long) extends AnyVal with BaseId[Long]

object ArticleId {
  implicit val articleIdFormat: Format[ArticleId] = new Format[ArticleId] {
    override def reads(json: JsValue): JsResult[ArticleId] = Reads.LongReads.reads(json).map(ArticleId(_))

    override def writes(o: ArticleId): JsNumber = Writes.LongWrites.writes(o.id)
  }
}

object ArticleMetaModel extends IdMetaModel {
  val slug: Property[String] = Property("slug")
  val title: Property[String] = Property("title")
  val description: Property[String] = Property("description")
  val body: Property[String] = Property("body")

  val updatedAt: Property[Instant] = Property("updatedAt")

  override type ModelId = ArticleId
}