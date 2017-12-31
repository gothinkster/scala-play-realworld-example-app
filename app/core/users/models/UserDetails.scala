package core.users.models

import commons.models.{Email, Username}
import play.api.libs.json.{Format, Json}

case class UserDetails(email: Email, username: Username, bio: Option[String], image: Option[String])

object UserDetails {

  def apply(user: User): UserDetails = UserDetails(user.email, user.username, user.bio, user.image)

  implicit val userDetailsFormat: Format[UserDetails] = Json.format[UserDetails]
}