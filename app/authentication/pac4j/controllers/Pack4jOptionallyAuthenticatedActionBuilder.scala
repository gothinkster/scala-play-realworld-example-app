package authentication.pac4j.controllers

import authentication.exceptions.ExceptionWithCode
import authentication.repositories.SecurityUserRepo
import commons.repositories.DateTimeProvider
import commons.services.ActionRunner
import core.authentication.api._
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

private[authentication] class Pack4jOptionallyAuthenticatedActionBuilder(sessionStore: PlaySessionStore,
                                                                         parsers: PlayBodyParsers,
                                                                         dateTimeProvider: DateTimeProvider,
                                                                         jwtAuthenticator: JwtAuthenticator,
                                                                         securityUserRepo: SecurityUserRepo,
                                                                         actionRunner: ActionRunner)
                                                                        (implicit ec: ExecutionContext)
  extends AbstractPack4jAuthenticatedActionBuilder(sessionStore, dateTimeProvider, jwtAuthenticator, actionRunner,
    securityUserRepo) {

  override val parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default(parsers)

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A],
                              block: OptionallyAuthenticatedUserRequest[A] => Future[Result]): Future[Result] = {
    actionRunner.runTransactionally(authenticate(request))
      .map(emailAndToken => new AuthenticatedUserRequest(AuthenticatedUser(emailAndToken), request))
      .recover({
        case _: ExceptionWithCode =>
          new NotAuthenticatedUserRequest(request)
      })
      .flatMap(block)
  }

}