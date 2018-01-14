package core.users.models

import commons.models.Username
import play.api.libs.json.{Format, Json}

case class Profile(username: Username, bio: Option[String], image: Option[String], following: Boolean)

object Profile {

  implicit val profileFormat: Format[Profile] = Json.format[Profile]

}