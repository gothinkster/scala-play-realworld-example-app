package core.articles.models

import play.api.libs.json.{Format, Json}

case class ArticleWrapper(article: Article)

object ArticleWrapper {
  implicit val articleWrapperFormat: Format[ArticleWrapper] = Json.format[ArticleWrapper]
}