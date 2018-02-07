package core.articles.models

import java.time.Instant

import commons.models.{BaseId, IdMetaModel, Property, WithId}
import core.users.models.UserId
import play.api.libs.json._
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class Article(id: ArticleId,
                   slug: String,
                   title: String,
                   description: String,
                   body: String,
                   createdAt: Instant,
                   updatedAt: Instant,
                   authorId: UserId
                  )
  extends WithId[Long, ArticleId]

object Article {
  implicit val articleFormat: Format[Article] = Json.format[Article]
}

case class ArticleId(override val value: Long) extends AnyVal with BaseId[Long]

object ArticleId {
  implicit val articleIdFormat: Format[ArticleId] = new Format[ArticleId] {
    override def reads(json: JsValue): JsResult[ArticleId] = Reads.LongReads.reads(json).map(ArticleId(_))

    override def writes(o: ArticleId): JsNumber = Writes.LongWrites.writes(o.value)
  }

  implicit val articleIdDbMapping: BaseColumnType[ArticleId] = MappedColumnType.base[ArticleId, Long](
    vo => vo.value,
    id => ArticleId(id)
  )
}

object ArticleMetaModel extends IdMetaModel {
  val slug: Property[String] = Property("slug")
  val title: Property[String] = Property("title")
  val description: Property[String] = Property("description")
  val body: Property[String] = Property("body")

  val createdAt: Property[Instant] = Property("createdAt")
  val updatedAt: Property[Instant] = Property("updatedAt")

  val userId: Property[UserId] = Property("userId")

  override type ModelId = ArticleId
}