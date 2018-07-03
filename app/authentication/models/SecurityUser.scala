package authentication.models

import java.time.Instant

import commons.models.{BaseId, Email, WithId}
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class SecurityUser(id: SecurityUserId,
                        email: Email,
                        password: PasswordHash,
                        createdAt: Instant,
                        updatedAt: Instant)
  extends WithId[Long, SecurityUserId] {

  override def toString: String = {
    s"SecurityUser($id, $email, password hash concealed, $createdAt, $updatedAt)"
  }

}

case class PasswordHash(value: String) extends AnyVal

object PasswordHash {

  implicit val passwordMapping: BaseColumnType[PasswordHash] = MappedColumnType.base[PasswordHash, String](
    password => password.value,
    str => PasswordHash(str)
  )

}

case class SecurityUserId(override val value: Long) extends AnyVal with BaseId[Long]

object SecurityUserId {

  implicit val securityUserIdDbMapping: BaseColumnType[SecurityUserId] =
    MappedColumnType.base[SecurityUserId, Long](
      vo => vo.value,
      id => SecurityUserId(id)
    )

}