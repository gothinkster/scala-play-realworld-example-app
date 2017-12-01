package core.authentication.api

import java.time.Instant

import commons.models.{Email, WithDateTimes}
import commons.repositories.{BaseId, WithId}

case class SecurityUser(id: SecurityUserId,
                        email: Email,
                        password: PasswordHash,
                        override val createdAt: Instant,
                        override val updatedAt: Instant)
  extends WithId[Long, SecurityUserId]
    with WithDateTimes[SecurityUser] {

  def updateCreatedAt(dateTime: Instant): SecurityUser = copy(createdAt = dateTime)

  def updateUpdatedAt(dateTime: Instant): SecurityUser = copy(updatedAt = dateTime)

  override def toString: String = {
    s"SecurityUser($id, $email, password hash concealed, $createdAt, $updatedAt)"
  }
}

case class PasswordHash(value: String) extends AnyVal

case class SecurityUserId(override val id: Long) extends AnyVal with BaseId[Long]