package users.controllers

import authentication.exceptions.{AuthenticationExceptionCode, ExceptionWithCode}
import authentication.jwt.services.JwtAuthenticator
import authentication.models.NotAuthenticatedUserRequest
import commons.services.ActionRunner
import play.api.libs.json.Json
import play.api.mvc
import play.api.mvc.Results._
import play.api.mvc._
import users.authentication.AuthenticatedUser
import users.repositories.UserRepo

import scala.concurrent.{ExecutionContext, Future}

private[users] class JwtAuthenticatedActionBuilder(
                                                    parsers: PlayBodyParsers,
                                                    jwtAuthenticator: JwtAuthenticator,
                                                    userRepo: UserRepo,
                                                    actionRunner: ActionRunner
                                                              )
                                                           (implicit ec: ExecutionContext)
  extends BaseActionBuilder(jwtAuthenticator, userRepo) with AuthenticatedActionBuilder {

  override val parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default(parsers)

  override protected def executionContext: ExecutionContext = ec

  private def onUnauthorized(exceptionCode: AuthenticationExceptionCode, requestHeader: RequestHeader) = {
    val response = HttpExceptionResponse(exceptionCode)
    Unauthorized(Json.toJson(response))
  }

  override def invokeBlock[A](request: Request[A],
                              block: AuthenticatedUserRequest[A] => Future[Result]): Future[Result] = {
    actionRunner.runTransactionally(authenticate(request))
      .flatMap(userAndToken => {
        val authenticatedRequest = new AuthenticatedUserRequest(AuthenticatedUser(userAndToken), request)
        block(authenticatedRequest)
      })
      .recover({
        case e: ExceptionWithCode =>
          onUnauthorized(e.exceptionCode, request)
      })
  }

}

