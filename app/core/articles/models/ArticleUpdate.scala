package core.articles.models

import play.api.libs.json.{Format, Json}

case class ArticleUpdate(title: Option[String] = None,
                         description: Option[String] = None,
                         body: Option[String] = None)

object ArticleUpdate {

  implicit val articleUpdateFormat: Format[ArticleUpdate] = Json.format[ArticleUpdate]

}