package core.articles.models

import commons.repositories.DateTimeProvider
import core.users.models.UserId
import play.api.libs.json.{Format, Json}

case class NewArticle(slug: String, title: String, description: String, body: String, tags: Seq[String]) {
  def toArticle(userId: UserId, dateTimeProvider: DateTimeProvider): Article = {
    val now = dateTimeProvider.now
    Article(ArticleId(-1), slug, title, description, body, now, now, userId)
  }
}

object NewArticle {
  implicit val newArticleFormat: Format[NewArticle] = Json.format[NewArticle]
}