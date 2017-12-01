package core.users.models

import commons.models.{Email, Username}
import core.authentication.api.PlainTextPassword
import play.api.libs.json.{Format, Json}

private[users] case class UserRegistration(username: Username, password: PlainTextPassword, email: Email)

object UserRegistration {
  implicit val userRegistrationFormat: Format[UserRegistration] = Json.format[UserRegistration]
}