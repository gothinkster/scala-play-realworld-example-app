package authentication.services

import authentication.repositories.SecurityUserRepo
import commons.models.Email
import commons.repositories.{ActionRunner, DateTimeProvider}
import core.authentication.api._
import org.mindrot.jbcrypt.BCrypt
import slick.dbio.DBIO

private[authentication] class SecurityUserService(securityUserRepo: SecurityUserRepo,
                                                  dateTimeProvider: DateTimeProvider,
                                                  actionRunner: ActionRunner)
  extends SecurityUserProvider
    with SecurityUserCreator
    with SecurityUserUpdater {

  override def create(newSecUser: NewSecurityUser): DBIO[SecurityUser] = {
    require(newSecUser != null)

    val passwordHash = hashPass(newSecUser.password)
    val now = dateTimeProvider.now
    securityUserRepo.insertAndGet(SecurityUser(SecurityUserId(-1), newSecUser.email, passwordHash, now, now))
  }

  private def hashPass(password: PlainTextPassword): PasswordHash = {
    val hash = BCrypt.hashpw(password.value, BCrypt.gensalt())
    PasswordHash(hash)
  }

  override def byEmail(email: Email): DBIO[Option[SecurityUser]] = {
    require(email != null)

    securityUserRepo.byEmail(email)
  }

  override def updateEmail(securityUser: SecurityUser, newEmail: Email): DBIO[SecurityUser] = {
    require(securityUser != null && newEmail != null)

    securityUserRepo.updateAndGet(securityUser.copy(email = newEmail))
  }

  override def updatePassword(securityUser: SecurityUser, newPassword: PlainTextPassword): DBIO[SecurityUser] = {
    require(securityUser != null && newPassword != null)

    val hash = hashPass(newPassword)
    securityUserRepo.updateAndGet(securityUser.copy(password = hash))
  }
}
