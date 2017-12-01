package core.users.models

import java.time.Instant

import commons.models._
import commons.repositories.{BaseId, WithId}
import play.api.libs.json._

case class User(id: UserId, username: Username, email: Email,
                override val createdAt: Instant,
                override val updatedAt: Instant) extends WithId[Long, UserId] with WithDateTimes[User] {

  override def updateCreatedAt(dateTime: Instant): User = copy(createdAt = dateTime)

  override def updateUpdatedAt(dateTime: Instant): User = copy(updatedAt = dateTime)
}

object User {
  implicit val userFormat: Format[User] = Json.format[User]
}

case class UserId(override val id: Long) extends AnyVal with BaseId[Long]

object UserId {
  implicit val userIdFormat: Format[UserId] = new Format[UserId] {
    override def reads(json: JsValue): JsResult[UserId] = Reads.LongReads.reads(json).map(UserId(_))

    override def writes(o: UserId): JsValue = Writes.LongWrites.writes(o.id)
  }
}

object UserMetaModel extends IdMetaModel {
  override type ModelId = UserId

  val username: Property[Username] = Property("username")
  val email: Property[Email] = Property("email")
}
