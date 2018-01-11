package core.users.models

import play.api.libs.json.{Format, Json}

case class UpdateUserWrapper(user: UserUpdate)

object UpdateUserWrapper {

  implicit val updateUserWrapperFormat: Format[UpdateUserWrapper] = Json.format[UpdateUserWrapper]

}

