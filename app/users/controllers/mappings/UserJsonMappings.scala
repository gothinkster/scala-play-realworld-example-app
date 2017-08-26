package users.controllers.mappings

import play.api.libs.json._
import users.models.{User, UserId}

object UserJsonMappings {

  import commons.controllers.mappings.LoginJsonMappings._

  implicit val userIdReads: Reads[UserId] = Reads((Reads.LongReads.reads(_)).andThen(_.map(UserId)))
  implicit val userIdWrites: Writes[UserId] = Writes((Writes.LongWrites.writes(_)).compose(_.value))

  implicit val userReads: Reads[User] = Json.reads[User]
  implicit val userWrites: Writes[User] = Json.writes[User]
}
