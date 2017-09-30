package authentication.controllers

import authentication.models.BearerTokenResponse
import commons.models.MissingOrInvalidCredentialsCode
import commons.repositories.{ActionRunner, DateTimeProvider}
import core.commons.controllers.RealWorldAbstractController
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
                               dateTimeProvider: DateTimeProvider,
                               jwtGenerator: JwtGenerator[CommonProfile])(implicit private val ec: ExecutionContext)
                                extends RealWorldAbstractController(components) {

  private val client = new DirectBasicAuthClient(httpBasicAuthenticator)

  def authenticate: Action[AnyContent] = Action { request =>
    val webContext = new PlayWebContext(request, sessionStore)

    Option(client.getCredentials(webContext))
      .map(credentials => {
        val profile = new JwtProfile()
        profile.setId(credentials.getUsername)

        val jwtToken = jwtGenerator.generate(profile)
        // todo expiration mechanism
        val json = Json.toJson(BearerTokenResponse(jwtToken, dateTimeProvider.now))
        Ok(json)
      })
      .getOrElse(Forbidden(Json.toJson(HttpExceptionResponse(MissingOrInvalidCredentialsCode))))
  }
}