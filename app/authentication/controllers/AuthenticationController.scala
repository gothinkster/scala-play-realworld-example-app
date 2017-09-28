package authentication.controllers

import commons.models.MissingOrInvalidCredentialsCode
import commons.repositories.ActionRunner
import core.authentication.api.BearerTokenResponse
import core.commons.models.HttpExceptionResponse
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.DirectBasicAuthClient
import org.pac4j.jwt.profile.{JwtGenerator, JwtProfile}
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.ExecutionContext

class AuthenticationController(actionRunner: ActionRunner,
                               sessionStore: PlaySessionStore,
                               httpBasicAuthenticator: Authenticator[UsernamePasswordCredentials],
                               components: ControllerComponents,
                               jwtGenerator: JwtGenerator[CommonProfile])(implicit private val ec: ExecutionContext)
                                extends AbstractController(components) {

  private val client = new DirectBasicAuthClient(httpBasicAuthenticator)

  def authenticate: Action[AnyContent] = Action { request =>
    val webContext = new PlayWebContext(request, sessionStore)

    Option(client.getCredentials(webContext))
      .map(credentials => {
        val profile = new JwtProfile()
        profile.setId(credentials.getUsername)

        val jwtToken = jwtGenerator.generate(profile)
        val json = Json.toJson(BearerTokenResponse(jwtToken))
        Ok(json)
      })
      .getOrElse(Forbidden(Json.toJson(HttpExceptionResponse(MissingOrInvalidCredentialsCode))))
  }
}