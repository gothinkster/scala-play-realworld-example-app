package core.authentication.api

import commons.models.Email
import play.api.libs.json.{Format, Json}

case class AuthenticatedUser(email: Email)

object AuthenticatedUser {
  implicit val authenticatedUserFormat: Format[AuthenticatedUser] = Json.format[AuthenticatedUser]
}