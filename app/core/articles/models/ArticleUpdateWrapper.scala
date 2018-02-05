package core.articles.models

import play.api.libs.json.{Format, Json}

case class ArticleUpdateWrapper(article: ArticleUpdate)

object ArticleUpdateWrapper {

  implicit val articleUpdateWrapperFormat: Format[ArticleUpdateWrapper] = Json.format[ArticleUpdateWrapper]

}