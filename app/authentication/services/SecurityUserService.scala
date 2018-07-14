package authentication.services

import authentication.api._
import authentication.models
import authentication.models._
import authentication.repositories.SecurityUserRepo
import commons.models.Email
import commons.repositories.DateTimeProvider
import commons.services.ActionRunner
import org.mindrot.jbcrypt.BCrypt
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[authentication] class SecurityUserService(securityUserRepo: SecurityUserRepo,
                                                  dateTimeProvider: DateTimeProvider,
                                                  actionRunner: ActionRunner,
                                                  implicit private val ec: ExecutionContext)
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

  override def update(currentEmail: Email, securityUserUpdate: SecurityUserUpdate): DBIO[SecurityUser] = {
    require(currentEmail != null && securityUserUpdate != null)

    for {
      securityUser <- findByEmail(currentEmail)
      withUpdatedEmail <- maybeUpdateEmail(securityUser, securityUserUpdate.email)
      withUpdatedPassword <- maybeUpdatePassword(withUpdatedEmail, securityUserUpdate.password)
    } yield withUpdatedPassword
  }

  override def findByEmail(email: Email): DBIO[SecurityUser] = {
    require(email != null)

    securityUserRepo.findByEmail(email)
  }

  private def maybeUpdateEmail(securityUser: SecurityUser, maybeEmail: Option[Email]) = {
    maybeEmail.filter(_ != securityUser.email)
      .map(newEmail => {
        securityUserRepo.updateAndGet(securityUser.copy(email = newEmail))
      }).getOrElse(DBIO.successful(securityUser))
  }

  private def maybeUpdatePassword(securityUser: SecurityUser, maybeNewPassword: Option[PlainTextPassword]) = {
    maybeNewPassword.map(newPassword => {
      val hash = hashPass(newPassword)
      securityUserRepo.updateAndGet(securityUser.copy(password = hash))
    }).getOrElse(DBIO.successful(securityUser))
  }

}
