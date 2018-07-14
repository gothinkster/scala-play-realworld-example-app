package authentication.models

import commons.models.Email
import play.api.libs.json.{Format, Json}

case class AuthenticatedUser(email: Email, token: String)

object AuthenticatedUser {

  def apply(emailAndToken: (Email, String)): AuthenticatedUser = AuthenticatedUser(emailAndToken._1, emailAndToken._2)

  implicit val authenticatedUserFormat: Format[AuthenticatedUser] = Json.format[AuthenticatedUser]
}