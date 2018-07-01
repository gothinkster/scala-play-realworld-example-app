package articles.models

import play.api.libs.json.{Format, Json}

case class ArticleWrapper(article: ArticleWithTags)

object ArticleWrapper {
  implicit val articleWrapperFormat: Format[ArticleWrapper] = Json.format[ArticleWrapper]
}