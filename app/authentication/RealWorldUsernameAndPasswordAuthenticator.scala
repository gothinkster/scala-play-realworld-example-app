package authentication

import authentication.exceptions.{InvalidPasswordException, MissingSecurityUserException}
import authentication.repositories.SecurityUserRepo
import commons.models.Email
import commons.repositories.ActionRunner
import commons.utils.DbioUtils.optionToDbio
import core.authentication.api.SecurityUser
import org.mindrot.jbcrypt.BCrypt
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.exception.CredentialsException

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

private[authentication] class RealWorldUsernameAndPasswordAuthenticator(actionRunner: ActionRunner,
                                                                        securityUserRepo: SecurityUserRepo)(implicit private val ec: ExecutionContext)
  extends Authenticator[UsernamePasswordCredentials] {

  override def validate(credentials: UsernamePasswordCredentials, context: WebContext): Unit = {
    require(credentials != null && context != null)

    val email = credentials.getUsername
    val validateAction = securityUserRepo.byEmail(Email(email))
      .flatMap(optionToDbio(_, new CredentialsException(new MissingSecurityUserException(email))))
      .map(user => {
        if (authenticated(credentials.getPassword, user)) user
        else throw new CredentialsException(new InvalidPasswordException)
      })

    Await.result(actionRunner.runInTransaction(validateAction), DurationInt(1).minute)
  }

  private def authenticated(givenPassword: String, secUsr: SecurityUser) = {
    BCrypt.checkpw(givenPassword, secUsr.password.value)
  }

}