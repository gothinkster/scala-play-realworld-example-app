package users.controllers

import authentication.exceptions.ExceptionWithCode
import authentication.jwt.services.JwtAuthenticator
import authentication.models.NotAuthenticatedUserRequest
import commons.services.ActionRunner
import play.api.mvc
import play.api.mvc._
import users.authentication.AuthenticatedUser
import users.repositories.UserRepo

import scala.concurrent.{ExecutionContext, Future}

private[users] class JwtOptionallyAuthenticatedActionBuilder(
                                                              parsers: PlayBodyParsers,
                                                              jwtAuthenticator: JwtAuthenticator,
                                                              userRepo: UserRepo,
                                                              actionRunner: ActionRunner
                                                                  )(implicit ec: ExecutionContext)
  extends BaseActionBuilder(jwtAuthenticator, userRepo) with OptionallyAuthenticatedActionBuilder {

  override val parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default(parsers)

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A],
                              block: OptionallyAuthenticatedUserRequest[A] => Future[Result]): Future[Result] = {
    val authenticateAction = actionRunner.runTransactionally(authenticate(request))

    authenticateAction
        .map(securityUserIdAndToken => new AuthenticatedUserRequest(AuthenticatedUser(securityUserIdAndToken), request))
        .recover({
          case _: ExceptionWithCode =>
            new NotAuthenticatedUserRequest(request)
        })
        .flatMap(block)
  }

}