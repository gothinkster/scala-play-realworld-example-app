package authentication.models

import commons.models.Login
import play.api.libs.json.{Format, Json}

case class AuthenticatedUser(login: Login)

object AuthenticatedUser {
  implicit val jsonFormat: Format[AuthenticatedUser] = Json.format[AuthenticatedUser]
}