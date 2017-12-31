package core.users.models

import play.api.libs.json.{Format, Json}

case class UserDetailsWrapper(user: UserDetails)

object UserDetailsWrapper {
  implicit val userDetailsWrapperFormat: Format[UserDetailsWrapper] = Json.format[UserDetailsWrapper]
}