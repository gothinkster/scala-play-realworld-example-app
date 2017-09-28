package authentication.services

import authentication.repositories.SecurityUserRepo

import commons.models.Login
import core.authentication.api._
import org.mindrot.jbcrypt.BCrypt
import slick.dbio.DBIO

private[authentication] class SecurityUserService(securityUserRepo: SecurityUserRepo)
  extends SecurityUserProvider
    with SecurityUserCreator {

  override def create(newSecUser: NewSecurityUser): DBIO[SecurityUser] = {
    require(newSecUser != null)

    val passwordHash = hashPass(newSecUser.password)
    securityUserRepo.create(SecurityUser(SecurityUserId(-1), newSecUser.login, passwordHash, null, null))
  }

  private def hashPass(password: PlainTextPassword): PasswordHash = {
    val hash = BCrypt.hashpw(password.value, BCrypt.gensalt())
    PasswordHash(hash)
  }

  override def byLogin(login: Login): DBIO[Option[SecurityUser]] = {
    require(login != null)

    securityUserRepo.byLogin(login)
  }
}