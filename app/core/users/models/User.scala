package core.users.models

import java.time.Instant

import commons.models.{WithId, _}
import play.api.libs.json._
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class User(id: UserId,
                username: Username,
                email: Email,
                bio: Option[String],
                image: Option[String],
                createdAt: Instant,
                updatedAt: Instant) extends WithId[Long, UserId]

object User {
  implicit val userFormat: Format[User] = Json.format[User]
}

case class UserId(override val value: Long) extends AnyVal with BaseId[Long]

object UserId {
  implicit val userIdFormat: Format[UserId] = new Format[UserId] {
    override def reads(json: JsValue): JsResult[UserId] = Reads.LongReads.reads(json).map(UserId(_))

    override def writes(o: UserId): JsValue = Writes.LongWrites.writes(o.value)
  }

  implicit val userIdDbMapping: BaseColumnType[UserId] = MappedColumnType.base[UserId, Long](
    vo => vo.value,
    id => UserId(id)
  )

}

object UserMetaModel extends IdMetaModel {
  override type ModelId = UserId

  val username: Property[Username] = Property("username")
  val email: Property[Email] = Property("email")
  val bio: Property[Option[String]] = Property("bio")
  val image: Property[Option[String]] = Property("image")
}
