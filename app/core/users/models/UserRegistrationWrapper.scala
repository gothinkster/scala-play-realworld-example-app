package core.users.models

import play.api.libs.json.{Format, Json}

private[users] case class UserRegistrationWrapper(user: UserRegistration)

object UserRegistrationWrapper {
  implicit val userRegistrationWrapperFormat: Format[UserRegistrationWrapper] = Json.format[UserRegistrationWrapper]
}