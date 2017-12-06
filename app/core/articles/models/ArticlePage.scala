package core.articles.models

import play.api.libs.json.{Format, Json}

case class ArticlePage(articles: Seq[ArticleWithTags], articlesCount: Long)

object ArticlePage {
  implicit val articlePageFormat: Format[ArticlePage] = Json.format[ArticlePage]
}