package core.users.models

import java.time.Instant

import commons.models.{Email, Username}
import play.api.libs.json.{Format, Json}

private[users] case class RegisteredUser(email: Email, username: Username, createdAt: Instant,
                                         updatedAt: Instant, token: String)

object RegisteredUser {
  implicit val registeredUserFormat: Format[RegisteredUser] = Json.format[RegisteredUser]
}

private[users] case class RegisteredUserWrapper(user: RegisteredUser)

object RegisteredUserWrapper {
  implicit val registeredUserWrapperFormat: Format[RegisteredUserWrapper] = Json.format[RegisteredUserWrapper]
}