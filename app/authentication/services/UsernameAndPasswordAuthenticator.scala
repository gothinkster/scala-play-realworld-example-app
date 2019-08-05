package authentication.services

import authentication.api._
import authentication.exceptions.InvalidPasswordException
import authentication.models._
import org.mindrot.jbcrypt.BCrypt
import play.api.mvc.Request
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[authentication] class UsernameAndPasswordAuthenticator(
                                                       tokenGenerator: TokenGenerator[IdProfile, JwtToken],
                                                       securityUserProvider: SecurityUserProvider
                                                     )
                                                     (implicit private val ec: ExecutionContext)
  extends Authenticator[CredentialsWrapper] {

  override def authenticate(request: Request[CredentialsWrapper]): DBIO[String] = {
    require(request != null)

    val EmailAndPasswordCredentials(email, password) = request.body.user

    securityUserProvider.findByEmail(email)
      .map(securityUser => {
        if (authenticated(password, securityUser))
          tokenGenerator.generate(IdProfile(securityUser.id)).token
        else
          throw new InvalidPasswordException(email.toString)
      })
  }

  private def authenticated(givenPassword: PlainTextPassword, secUsr: SecurityUser) = {
    BCrypt.checkpw(givenPassword.value, secUsr.password.value)
  }

}