package authentication.services

import authentication.repositories.SecurityUserRepo
import commons.models.Email
import commons.repositories.DateTimeProvider
import commons.services.ActionRunner
import authentication.api._
import authentication.models
import authentication.models._
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
    securityUserRepo.insertAndGet(models.SecurityUser(SecurityUserId(-1), newSecUser.email, passwordHash, now, now))
  }

  private def hashPass(password: PlainTextPassword): PasswordHash = {
    val hash = BCrypt.hashpw(password.value, BCrypt.gensalt())
    PasswordHash(hash)
  }

  override def findByEmailOption(email: Email): DBIO[Option[SecurityUser]] = {
    require(email != null)

    securityUserRepo.findByEmailOption(email)
  }

  override def findByEmail(email: Email): DBIO[SecurityUser] = {
    require(email != null)

    securityUserRepo.findByEmail(email)
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
