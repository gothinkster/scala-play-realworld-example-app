package users.controllers.mappings

import authentication.models.api.PlainTextPassword
import play.api.libs.json.{Json, Reads, Writes}
import users.models.UserRegistration

object UserRegistrationJsonMappings {

  import commons.controllers.mappings.LoginJsonMappings._

  implicit val planTextPasswordReads: Reads[PlainTextPassword] =
    Reads((Reads.StringReads.reads(_)).andThen(_.map(PlainTextPassword)))
  implicit val planTextPasswordWrites: Writes[PlainTextPassword] =
    Writes((Writes.StringWrites.writes(_)).compose(_.value))

  implicit val userRegistrationReads: Reads[UserRegistration] = Json.reads[UserRegistration]
  implicit val userRegistrationWrites: Writes[UserRegistration] = Json.writes[UserRegistration]
}
