package authentication.models

import java.time.LocalDateTime

import commons.models.{Login, WithDateTimes}
import commons.repositories.{BaseId, WithId}

private[authentication] case class SecurityUser(id: SecurityUserId,
                        login: Login,
                        password: PasswordHash,
                        override val createdAt: LocalDateTime,
                        override val modifiedAt: LocalDateTime)
  extends WithId[Long, SecurityUserId]
  with WithDateTimes[SecurityUser] {

  def updateCreatedAt(dateTime: LocalDateTime): SecurityUser = copy(createdAt = dateTime)

  def updateModifiedAt(dateTime: LocalDateTime): SecurityUser = copy(modifiedAt = dateTime)

  override def toString: String = {
    s"SecurityUser($id, $login, password hash concealed, $createdAt, $modifiedAt)"
  }
}

private[authentication] case class PasswordHash(value: String) extends AnyVal

private[authentication] case class SecurityUserId(override val id: Long) extends AnyVal with BaseId[Long]