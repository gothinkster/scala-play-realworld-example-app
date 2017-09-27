package articles.models

import java.time.LocalDateTime

import commons.models.{IdMetaModel, Property, WithDateTimes}
import commons.repositories.{BaseId, WithId}

case class Article(id: ArticleId,
                   slug: String,
                   title: String,
                   description: String,
                   body: String,
                   override val createdAt: LocalDateTime,
                   override val modifiedAt: LocalDateTime,
                  )
  extends WithId[Long, ArticleId]
    with WithDateTimes[Article] {

  override def updateCreatedAt(dateTime: LocalDateTime): Article = copy(createdAt = dateTime)

  override def updateModifiedAt(dateTime: LocalDateTime): Article = copy(modifiedAt = dateTime)

}

case class ArticleId(override val id: Long) extends AnyVal with BaseId[Long]

object ArticleMetaModel extends IdMetaModel {
  val slug: Property[String] = Property("slug")
  val title: Property[String] = Property("title")
  val description: Property[String] = Property("description")
  val body: Property[String] = Property("body")

  val modifiedAt: Property[LocalDateTime] = Property("modifiedAt")

  override type ModelId = ArticleId
}