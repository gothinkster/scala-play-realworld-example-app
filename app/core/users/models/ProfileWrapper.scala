package core.users.models

import play.api.libs.json.{Format, Json}

private[users] case class ProfileWrapper(profile: Profile)

private[users] object ProfileWrapper {

  implicit val profileWrapperFormat: Format[ProfileWrapper] = Json.format[ProfileWrapper]

}