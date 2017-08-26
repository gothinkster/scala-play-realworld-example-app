package authentication.oauth2

import java.time.LocalDateTime

import authentication.models.SecurityUserId
import commons.models.WithDateTimes
import commons.repositories.{BaseId, WithId}

private[authentication] case class AccessToken(id: AccessTokenId,
                       securityUserId: SecurityUserId,
                       token: String,
                       override val createdAt: LocalDateTime,
                       override val modifiedAt: LocalDateTime)
  extends WithId[Long, AccessTokenId]
  with WithDateTimes[AccessToken] {

  def updateCreatedAt(dateTime: LocalDateTime): AccessToken = copy(createdAt = dateTime)

  def updateModifiedAt(dateTime: LocalDateTime): AccessToken = copy(modifiedAt = dateTime)
}

private[authentication] case class AccessTokenId(override val id: Long) extends AnyVal with BaseId[Long]