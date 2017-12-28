package authentication.controllers

import authentication.models.BearerTokenResponse
import commons.models.MissingOrInvalidCredentialsCode
import commons.repositories.ActionRunner
import core.authentication.api.{EmailProfile, JwtToken, RealWorldAuthenticator}
import core.commons.controllers.RealWorldAbstractController
import core.commons.models.HttpExceptionResponse
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.http.client.direct.DirectBasicAuthClient
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.libs.json._
import play.api.mvc._

class AuthenticationController(actionRunner: ActionRunner,
                               sessionStore: PlaySessionStore,
                               httpBasicAuthenticator: org.pac4j.core.credentials.authenticator.Authenticator[UsernamePasswordCredentials],
                               components: ControllerComponents,
                               pack4jJwtAuthenticator: RealWorldAuthenticator[EmailProfile, JwtToken]
                              )
  extends RealWorldAbstractController(components) {

  private val client = new DirectBasicAuthClient(httpBasicAuthenticator)

  def authenticate: Action[AnyContent] = Action { request =>
    val webContext = new PlayWebContext(request, sessionStore)

    Option(client.getCredentials(webContext))
      .map(credentials => {
        val profile = new EmailProfile(credentials.getUsername)
        val jwtToken = pack4jJwtAuthenticator.authenticate(profile)

        BearerTokenResponse(jwtToken.token, jwtToken.expiredAt)
      })
      .map(Json.toJson(_))
      .map(Ok(_))
      .getOrElse(Forbidden(Json.toJson(HttpExceptionResponse(MissingOrInvalidCredentialsCode))))
  }

}