package core.articles.controllers.mappings

import core.articles.models.{Tag, TagId, TagListWrapper}
import core.commons.controllers.mappings.DateTimeJsonMappings
import play.api.libs.json._

trait TagJsonMappings extends DateTimeJsonMappings {
  implicit val tagIdReads: Reads[TagId] = Reads((Reads.LongReads.reads(_)).andThen(_.map(TagId)))
  implicit val tagIdWrites: Writes[TagId] = Writes((Writes.LongWrites.writes(_)).compose(_.value))

  implicit val tagFormat: Format[Tag] = Json.format[Tag]

  implicit val tagWrapperFormat: Format[TagListWrapper] = Json.format[TagListWrapper]
}