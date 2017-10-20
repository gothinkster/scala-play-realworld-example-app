package authentication.services

import authentication.repositories.SecurityUserRepo
import commons.models.Email
import commons.repositories.ActionRunner
import core.authentication.api._
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future

private[authentication] class SecurityUserService(securityUserRepo: SecurityUserRepo,
                                                  actionRunner: ActionRunner
                                                 )
  extends SecurityUserProvider
    with SecurityUserCreator {

  override def create(newSecUser: NewSecurityUser): Future[SecurityUser] = {
    require(newSecUser != null)

    val passwordHash = hashPass(newSecUser.password)
    val action = securityUserRepo.create(SecurityUser(SecurityUserId(-1), newSecUser.email, passwordHash, null, null))

    actionRunner.runInTransaction(action)
  }

  private def hashPass(password: PlainTextPassword): PasswordHash = {
    val hash = BCrypt.hashpw(password.value, BCrypt.gensalt())
    PasswordHash(hash)
  }

  override def byEmail(email: Email): Future[Option[SecurityUser]] = {
    require(email != null)

    val action = securityUserRepo.byEmail(email)
    actionRunner.runInTransaction(action)
  }
}
