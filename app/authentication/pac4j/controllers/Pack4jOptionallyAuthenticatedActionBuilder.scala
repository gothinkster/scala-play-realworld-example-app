package authentication.pac4j.controllers

import authentication.exceptions.WithExceptionCode
import commons.repositories.DateTimeProvider
import commons.services.ActionRunner
import core.authentication.api.{AuthenticatedUser, MaybeAuthenticatedUserRequest, OptionallyAuthenticatedActionBuilder, SecurityUserProvider}
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

private[authentication] class Pack4jOptionallyAuthenticatedActionBuilder(sessionStore: PlaySessionStore,
                                                                         parsers: PlayBodyParsers,
                                                                         dateTimeProvider: DateTimeProvider,
                                                                         jwtAuthenticator: JwtAuthenticator,
                                                                         securityUserProvider: SecurityUserProvider,
                                                                         actionRunner: ActionRunner)
                                                                        (implicit ec: ExecutionContext)
  extends AbstractPack4jAuthenticatedActionBuilder(sessionStore, dateTimeProvider, jwtAuthenticator, actionRunner,
    securityUserProvider) with OptionallyAuthenticatedActionBuilder {

  override val parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default(parsers)

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A],
                              block: (MaybeAuthenticatedUserRequest[A]) => Future[Result]): Future[Result] = {
    actionRunner.runTransactionally(authenticate(request))
      .map(email => Some(email))
      .recover({
        case _: WithExceptionCode =>
          None
      })
      .map(maybeEmail => new MaybeAuthenticatedUserRequest(maybeEmail.map(AuthenticatedUser(_)), request))
      .flatMap(block)
  }

}