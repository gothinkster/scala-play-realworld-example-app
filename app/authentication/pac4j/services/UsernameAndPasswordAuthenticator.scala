package authentication.pac4j.services

import authentication.api._
import authentication.exceptions.InvalidPasswordException
import authentication.models._
import authentication.repositories.SecurityUserRepo
import commons.services.ActionRunner
import org.mindrot.jbcrypt.BCrypt
import play.api.mvc.Request
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[authentication] class UsernameAndPasswordAuthenticator(tokenGenerator: TokenGenerator[SecurityUserIdProfile, JwtToken],
                                                               actionRunner: ActionRunner,
                                                               securityUserRepo: SecurityUserRepo)
                                                              (implicit private val ec: ExecutionContext)
  extends Authenticator[CredentialsWrapper] {

  override def authenticate(request: Request[CredentialsWrapper]): DBIO[String] = {
    require(request != null)

    val EmailAndPasswordCredentials(email, password) = request.body.user

    securityUserRepo.findByEmail(email)
      .map(user => {
        if (authenticated(password, user)) tokenGenerator.generate(SecurityUserIdProfile(user.id)).token
        else throw new InvalidPasswordException(email.toString)
      })
  }

  private def authenticated(givenPassword: PlainTextPassword, secUsr: SecurityUser) = {
    BCrypt.checkpw(givenPassword.value, secUsr.password.value)
  }

}