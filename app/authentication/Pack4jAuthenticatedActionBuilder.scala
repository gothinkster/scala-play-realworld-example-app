package authentication

import commons.models._
import commons.repositories.DateTimeProvider
import commons.utils.DateUtils
import core.authentication.api.{AuthenticatedActionBuilder, AuthenticatedUser, AuthenticatedUserRequest}
import core.commons.models.HttpExceptionResponse
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.HeaderClient
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.jwt.profile.JwtProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc._
import play.mvc.Http

import scala.concurrent.{ExecutionContext, Future}

private[authentication] class Pack4jAuthenticatedActionBuilder(sessionStore: PlaySessionStore,
                                       parse: PlayBodyParsers,
                                       dateTimeProvider: DateTimeProvider,
                                       jwtAuthenticator: JwtAuthenticator)(implicit ec: ExecutionContext)
  extends AuthenticatedActionBuilder {

  override val parser: BodyParser[AnyContent] = new BodyParsers.Default(parse)

  private val prefixSpaceIsCrucialHere = "Token "
  private val client = new HeaderClient(Http.HeaderNames.AUTHORIZATION, prefixSpaceIsCrucialHere, jwtAuthenticator)

  private def authenticate(requestHeader: RequestHeader) = {
    val webContext = new PlayWebContext(requestHeader, sessionStore)

    Option(client.getCredentials(webContext))
      .toRight(MissingOrInvalidCredentialsCode)
      .map(client.getUserProfile(_, webContext))
      .filterOrElse(isNotExpired, ExpiredCredentialsCode)
      .map(profile => AuthenticatedUser(Email(profile.getId)))
  }

  private def isNotExpired(profile: CommonProfile): Boolean =
    profile.isInstanceOf[JwtProfile] && isNotExpired(profile.asInstanceOf[JwtProfile])

  private def isNotExpired(profile: JwtProfile) = {
    val expirationDate = profile.getExpirationDate
    val expiredAt = DateUtils.toInstant(expirationDate)

    dateTimeProvider.now.isBefore(expiredAt)
  }

  override protected def executionContext: ExecutionContext = ec

  private def onUnauthorized(exceptionCode: ExceptionCode, requestHeader: RequestHeader) = {
    val response = HttpExceptionResponse(exceptionCode)
    Unauthorized(Json.toJson(response))
  }

  override def invokeBlock[A](request: Request[A], block: (AuthenticatedUserRequest[A]) => Future[Result]): Future[Result] = {
    authenticate(request) match {
      case Right(securityUser) => block(new AuthenticatedUserRequest(securityUser, request))
      case Left(code) => Future.successful(onUnauthorized(code, request))
    }
  }
}

