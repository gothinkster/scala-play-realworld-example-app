package authentication.pac4j.controllers

import authentication.exceptions.{AuthenticationExceptionCode, ExceptionWithCode}
import authentication.repositories.SecurityUserRepo
import commons.models._
import commons.repositories.DateTimeProvider
import commons.services.ActionRunner
import authentication.api._
import authentication.models.{AuthenticatedUser, AuthenticatedUserRequest, HttpExceptionResponse}
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
                                                               jwtAuthenticator: JwtAuthenticator,
                                                               securityUserRepo: SecurityUserRepo,
                                                               actionRunner: ActionRunner)
                                                              (implicit ec: ExecutionContext)
  extends AbstractPack4jAuthenticatedActionBuilder(sessionStore, dateTimeProvider, jwtAuthenticator, actionRunner,
    securityUserRepo) with AuthenticatedActionBuilder {

  override val parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default(parsers)

  override protected def executionContext: ExecutionContext = ec

  private def onUnauthorized(exceptionCode: AuthenticationExceptionCode, requestHeader: RequestHeader) = {
    val response = HttpExceptionResponse(exceptionCode)
    Unauthorized(Json.toJson(response))
  }

  override def invokeBlock[A](request: Request[A],
                              block: AuthenticatedUserRequest[A] => Future[Result]): Future[Result] = {
    actionRunner.runTransactionally(authenticate(request))
      .flatMap(emailAndToken => {
        val authenticatedUserRequest = new AuthenticatedUserRequest(AuthenticatedUser(emailAndToken), request)
        block(authenticatedUserRequest)
      })
      .recover({
        case e: ExceptionWithCode =>
          onUnauthorized(e.exceptionCode, request)
      })
  }

}

