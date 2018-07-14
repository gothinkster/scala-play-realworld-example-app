package users.models

import commons.models.Username
import play.api.libs.json._

case class Profile(userId: UserId, username: Username, bio: Option[String], image: Option[String], following: Boolean)

object Profile {

  def apply(user: User, following: Boolean): Profile = Profile(user.id, user.username, user.bio, user.image, following)

  implicit val profileFormat: Format[Profile] = new Format[Profile] {

    private val reads = new Reads[Profile] {
      override def reads(json: JsValue): JsResult[Profile] = {
        val username = json("username").as[Username]
        val bio = json("bio").asOpt[String]
        val image = json("image").asOpt[String]
        val following = json("following").as[Boolean]

        JsSuccess(Profile(UserId(-1), username, bio, image, following))
      }
    }

    private val writes = new Writes[Profile] {
      override def writes(profile: Profile): JsValue = {
        Json.obj(
          "username" -> profile.username,
          "bio" -> profile.bio,
          "image" -> profile.image,
          "following" -> profile.following
        )
      }
    }

    override def reads(json: JsValue): JsResult[Profile] = reads.reads(json)

    override def writes(o: Profile): JsValue = writes.writes(o)
  }

}