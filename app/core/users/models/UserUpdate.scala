package core.users.models

import commons.models.{Email, Username}
import core.authentication.api.PlainTextPassword
import play.api.libs.json.{Format, Json}

case class UserUpdate(email: Email, username: Username, bio: Option[String], image: Option[String],
                      newPassword: Option[PlainTextPassword])

object UserUpdate {

  implicit val updateUserFormat: Format[UserUpdate] = Json.format[UserUpdate]

}