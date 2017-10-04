package core.users.controllers.mappings

import authentication.controllers.mappings.AuthenticationJsonMappings
import commons.controllers.mappings.CommonJsonMappings
import core.authentication.controllers.mappings.AuthenticationApiJsonMappings
import core.commons.controllers.mappings.DateTimeJsonMappings
import play.api.libs.json._
import core.users.models.{User, UserId, UserRegistration, UserRegistrationWrapper}

trait UserJsonMappings extends CommonJsonMappings
  with AuthenticationApiJsonMappings
  with AuthenticationJsonMappings
  with DateTimeJsonMappings {

  implicit val userIdReads: Reads[UserId] = Reads((Reads.LongReads.reads(_)).andThen(_.map(UserId)))
  implicit val userIdWrites: Writes[UserId] = Writes((Writes.LongWrites.writes(_)).compose(_.value))

  implicit val userFormat: Format[User] = Json.format[User]

  implicit val userRegistrationFormat: Format[UserRegistration] = Json.format[UserRegistration]

  implicit val userRegistrationWrapperFormat: Format[UserRegistrationWrapper] = Json.format[UserRegistrationWrapper]
}
