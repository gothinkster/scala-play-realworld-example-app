package authentication.pac4j.services

import authentication.repositories.SecurityUserRepo
import commons.services.ActionRunner
import commons.utils.DbioUtils.optionToDbio
import core.authentication.api.{MissingSecurityUserException, _}
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

    val credentials = request.body.user

    val rawEmail = credentials.email.value
    securityUserRepo.findByEmail(credentials.email)
      .flatMap(optionToDbio(_, new MissingSecurityUserException(rawEmail)))
      .map(user => {
        if (authenticated(credentials.password, user)) tokenGenerator.generate(SecurityUserIdProfile(user.id)).token
        else throw new InvalidPasswordException(rawEmail)
      })
  }

  private def authenticated(givenPassword: PlainTextPassword, secUsr: SecurityUser) = {
    BCrypt.checkpw(givenPassword.value, secUsr.password.value)
  }

}