package core.users.models

import java.time.LocalDateTime

import commons.models._
import commons.repositories.{BaseId, WithId}

case class User(id: UserId, username: Username, email: Email,
                override val createdAt: LocalDateTime,
                override val modifiedAt: LocalDateTime) extends WithId[Long, UserId] with WithDateTimes[User] {

  override def updateCreatedAt(dateTime: LocalDateTime): User = copy(createdAt = dateTime)

  override def updateModifiedAt(dateTime: LocalDateTime): User = copy(modifiedAt = dateTime)
}

case class UserId(override val id: Long) extends AnyVal with BaseId[Long]

object UserMetaModel extends IdMetaModel {
  override type ModelId = UserId

  val username: Property[Username] = Property("username")
  val email: Property[Email] = Property("email")
}
