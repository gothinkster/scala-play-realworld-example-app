package users.models

import java.time.Instant

import commons.models.{Email, Username}
import play.api.libs.json.{Format, Json}

private[users] case class UserDetails(email: Email, username: Username, bio: Option[String], image: Option[String],
                       createdAt: Instant, updatedAt: Instant)

private[users] object UserDetails {

  def apply(user: User): UserDetails = UserDetails(user.email, user.username, user.bio, user.image, user.createdAt,
    user.updatedAt)

  implicit val userDetailsFormat: Format[UserDetails] = Json.format[UserDetails]
}