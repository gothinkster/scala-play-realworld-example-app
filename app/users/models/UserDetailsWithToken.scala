package users.models

import java.time.Instant

import commons.models.{Email, Username}
import play.api.libs.json.{Format, Json}

case class UserDetailsWithToken(email: Email, username: Username, createdAt: Instant, updatedAt: Instant,
                                               bio: Option[String], image: Option[String], token: String)

object UserDetailsWithToken {

  implicit val userDetailsWithTokenFormat: Format[UserDetailsWithToken] = Json.format[UserDetailsWithToken]

  def apply(userDetails: UserDetails, token: String): UserDetailsWithToken =
    UserDetailsWithToken(userDetails.email, userDetails.username, userDetails.createdAt, userDetails.updatedAt,
      userDetails.bio, userDetails.image, token)
}

case class UserDetailsWithTokenWrapper(user: UserDetailsWithToken)

object UserDetailsWithTokenWrapper {
  implicit val userDetailsWithTokenWrapperFormat: Format[UserDetailsWithTokenWrapper] =
    Json.format[UserDetailsWithTokenWrapper]
}