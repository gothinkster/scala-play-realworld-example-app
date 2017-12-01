package core.articles.models

import play.api.libs.json.{Format, Json}

case class NewArticleWrapper(article: NewArticle)

object NewArticleWrapper {
  implicit val newArticleWrapperFormat: Format[NewArticleWrapper] = Json.format[NewArticleWrapper]
}