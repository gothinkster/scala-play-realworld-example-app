package core.articles.models

import play.api.libs.json.{Format, Json}

case class ArticleUpdate(maybeTitle: Option[String] = None,
                         maybeDescription: Option[String] = None,
                         maybeBody: Option[String] = None)

object ArticleUpdate {

  implicit val articleUpdateFormat: Format[ArticleUpdate] = Json.format[ArticleUpdate]

}