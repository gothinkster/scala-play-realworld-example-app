package authentication.controllers

import authentication.models.{BearerTokenResponse, CredentialsWrapper}
import commons.models.MissingOrInvalidCredentialsCode
import commons.repositories.ActionRunner
import core.authentication.api.{EmailProfile, JwtToken, RealWorldAuthenticator}
import core.commons.controllers.RealWorldAbstractController
import core.commons.models.HttpExceptionResponse
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.libs.json._
import play.api.mvc._

class AuthenticationController(actionRunner: ActionRunner,
                               sessionStore: PlaySessionStore,
                               httpBasicAuthenticator: org.pac4j.core.credentials.authenticator.Authenticator[UsernamePasswordCredentials],
                               components: ControllerComponents,
                               pack4jJwtAuthenticator: RealWorldAuthenticator[EmailProfile, JwtToken])
  extends RealWorldAbstractController(components) {

  def authenticate: Action[CredentialsWrapper] = Action(validateJson[CredentialsWrapper]) { request =>
    val credentials = request.body.user
    val pack4jCredentials = new UsernamePasswordCredentials(credentials.email.value, credentials.password.value, "none")

    try {
      val webContext = new PlayWebContext(request, sessionStore)
      httpBasicAuthenticator.validate(pack4jCredentials, webContext)

      val profile = new EmailProfile(pack4jCredentials.getUsername)
      val jwtToken = pack4jJwtAuthenticator.authenticate(profile)

      val response = BearerTokenResponse(jwtToken.token, jwtToken.expiredAt)
      val json = Json.toJson(response)
      Ok(json)
    } catch {
      case _: CredentialsException =>
        Forbidden(Json.toJson(HttpExceptionResponse(MissingOrInvalidCredentialsCode)))
    }

  }

}