package authentication.services

import authentication.repositories.SecurityUserRepo
import commons.models.Email
import commons.repositories.ActionRunner
import core.authentication.api._
import org.mindrot.jbcrypt.BCrypt
import slick.dbio.DBIO

private[authentication] class SecurityUserService(securityUserRepo: SecurityUserRepo,
                                                  actionRunner: ActionRunner
                                                 )
  extends SecurityUserProvider
    with SecurityUserCreator {

  override def create(newSecUser: NewSecurityUser): DBIO[SecurityUser] = {
    require(newSecUser != null)

    val passwordHash = hashPass(newSecUser.password)
    securityUserRepo.create(SecurityUser(SecurityUserId(-1), newSecUser.email, passwordHash, null, null))
  }

  private def hashPass(password: PlainTextPassword): PasswordHash = {
    val hash = BCrypt.hashpw(password.value, BCrypt.gensalt())
    PasswordHash(hash)
  }

  override def byEmail(email: Email): DBIO[Option[SecurityUser]] = {
    require(email != null)

    securityUserRepo.byEmail(email)
  }
}
