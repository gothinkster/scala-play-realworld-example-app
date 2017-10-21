package core.authentication.api

import java.time.LocalDateTime

import commons.models.{Email, WithDateTimes}
import commons.repositories.{BaseId, WithId}

case class SecurityUser(id: SecurityUserId,
                        email: Email,
                        password: PasswordHash,
                        override val createdAt: LocalDateTime,
                        override val updatedAt: LocalDateTime)
  extends WithId[Long, SecurityUserId]
    with WithDateTimes[SecurityUser] {

  def updateCreatedAt(dateTime: LocalDateTime): SecurityUser = copy(createdAt = dateTime)

  def updateUpdatedAt(dateTime: LocalDateTime): SecurityUser = copy(updatedAt = dateTime)

  override def toString: String = {
    s"SecurityUser($id, $email, password hash concealed, $createdAt, $updatedAt)"
  }
}

case class PasswordHash(value: String) extends AnyVal

case class SecurityUserId(override val id: Long) extends AnyVal with BaseId[Long]