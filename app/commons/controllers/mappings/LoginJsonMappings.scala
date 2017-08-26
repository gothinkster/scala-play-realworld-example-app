package commons.controllers.mappings

import commons.models.Login
import play.api.libs.json._

object LoginJsonMappings {
  implicit val loginReads: Reads[Login] = Reads((Reads.StringReads.reads(_)).andThen(_.map(Login)))
  implicit val loginWrites: Writes[Login] = Writes((Writes.StringWrites.writes(_)).compose(_.value))
}