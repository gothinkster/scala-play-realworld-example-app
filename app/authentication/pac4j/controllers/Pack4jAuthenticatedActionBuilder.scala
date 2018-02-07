package authentication.pac4j.controllers

import commons.models._
import commons.repositories.DateTimeProvider
import core.authentication.api.{AuthenticatedActionBuilder, AuthenticatedUserRequest}
import core.commons.models.HttpExceptionResponse
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.play.store.PlaySessionStore
import play.api.libs.json.Json
import play.api.mvc
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

private[authentication] class Pack4jAuthenticatedActionBuilder(sessionStore: PlaySessionStore,
                                                               parsers: PlayBodyParsers,
                                                               dateTimeProvider: DateTimeProvider,
                                                               jwtAuthenticator: JwtAuthenticator)(implicit ec: ExecutionContext)
  extends AbstractPack4jAuthenticatedActionBuilder(sessionStore, dateTimeProvider, jwtAuthenticator)
    with AuthenticatedActionBuilder {

  override val parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default(parsers)

  override protected def executionContext: ExecutionContext = ec

  private def onUnauthorized(exceptionCode: ExceptionCode, requestHeader: RequestHeader) = {
    val response = HttpExceptionResponse(exceptionCode)
    Unauthorized(Json.toJson(response))
  }

  override def invokeBlock[A](request: Request[A],
                              block: (AuthenticatedUserRequest[A]) => Future[Result]): Future[Result] = {
    authenticate(request) match {
      case Right(authenticatedUser) => block(new AuthenticatedUserRequest(authenticatedUser, request))
      case Left(code) => Future.successful(onUnauthorized(code, request))
    }
  }

}

