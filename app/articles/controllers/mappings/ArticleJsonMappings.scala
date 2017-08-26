package articles.controllers.mappings

import articles.models.{Article, ArticleId}
import play.api.libs.json.{Format, Json, Reads, Writes}

object ArticleJsonMappings {

  implicit val articleIdReads: Reads[ArticleId] = Reads((Reads.LongReads.reads(_)).andThen(_.map(ArticleId)))
  implicit val articleIdWrites: Writes[ArticleId] = Writes((Writes.LongWrites.writes(_)).compose(_.value))

  implicit val articleFormat: Format[Article] = Json.format[Article]

}
