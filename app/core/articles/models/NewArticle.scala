package core.articles.models

import core.users.models.UserId
import play.api.libs.json.{Format, Json}

case class NewArticle(slug: String, title: String, description: String, body: String, author: UserId) {
  def toArticle: Article = {
    Article(ArticleId(-1), slug, title, description, body, null, null)
  }
}

object NewArticle {
  implicit val newArticleFormat: Format[NewArticle] = Json.format[NewArticle]
}