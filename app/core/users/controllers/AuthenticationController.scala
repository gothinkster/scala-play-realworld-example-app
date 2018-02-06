package core.users.controllers

import authentication.models.{CredentialsWrapper, EmailAndPasswordCredentials}
import commons.models.MissingOrInvalidCredentialsCode
import commons.repositories.ActionRunner
import core.authentication.api.{EmailProfile, JwtToken, RealWorldAuthenticator}
import core.commons.controllers.RealWorldAbstractController
import core.commons.models.HttpExceptionResponse
import core.users.models.{UserDetailsWithToken, UserDetailsWithTokenWrapper}
import core.users.services.UserService
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.util.Try

class AuthenticationController(actionRunner: ActionRunner,
                               sessionStore: PlaySessionStore,
                               httpBasicAuthenticator: org.pac4j.core.credentials.authenticator.Authenticator[UsernamePasswordCredentials],
                               components: ControllerComponents,
                               pack4jJwtAuthenticator: RealWorldAuthenticator[EmailProfile, JwtToken],
                               userService: UserService) extends RealWorldAbstractController(components) {

  def authenticate: Action[CredentialsWrapper] = Action.async(validateJson[CredentialsWrapper]) { request =>
    val credentials = request.body.user
    Future.fromTry(doAuthenticate(request, credentials))
      .flatMap(jwtToken => getUserDetailsWithToken(credentials, jwtToken.token))
      .map(UserDetailsWithTokenWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: CredentialsException =>
          Forbidden(Json.toJson(HttpExceptionResponse(MissingOrInvalidCredentialsCode)))
      })
  }

  private def getUserDetailsWithToken(credentials: EmailAndPasswordCredentials, token: String) = {
    actionRunner.runInTransaction(userService.getUserDetails(credentials.email))
      .map(userDetails => {
        UserDetailsWithToken(userDetails, token)
      })
  }

  private def doAuthenticate(request: Request[CredentialsWrapper], credentials: EmailAndPasswordCredentials) = Try {
    val webContext = new PlayWebContext(request, sessionStore)
    val pack4jCredentials = new UsernamePasswordCredentials(credentials.email.value, credentials.password.value, "none")
    httpBasicAuthenticator.validate(pack4jCredentials, webContext)

    val profile = new EmailProfile(pack4jCredentials.getUsername)
    pack4jJwtAuthenticator.authenticate(profile)
  }
}