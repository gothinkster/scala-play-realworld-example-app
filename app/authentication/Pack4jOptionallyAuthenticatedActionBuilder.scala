package authentication

import commons.repositories.DateTimeProvider
import core.authentication.api.{MaybeAuthenticatedUserRequest, OptionallyAuthenticatedActionBuilder}
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

private[authentication] class Pack4jOptionallyAuthenticatedActionBuilder(sessionStore: PlaySessionStore,
                                                                         parsers: PlayBodyParsers,
                                                                         dateTimeProvider: DateTimeProvider,
                                                                         jwtAuthenticator: JwtAuthenticator)
                                                                        (implicit ec: ExecutionContext)
  extends AbstractPack4jAuthenticatedActionBuilder(sessionStore, dateTimeProvider, jwtAuthenticator)
    with OptionallyAuthenticatedActionBuilder {

  override val parser: BodyParser[AnyContent] = new mvc.BodyParsers.Default(parsers)

  override protected def executionContext: ExecutionContext = ec

  override def invokeBlock[A](request: Request[A],
                              block: (MaybeAuthenticatedUserRequest[A]) => Future[Result]): Future[Result] = {
    val maybeAuthenticatedUser = authenticate(request).toOption
    val requestWrapper = new MaybeAuthenticatedUserRequest(maybeAuthenticatedUser, request)
    block(requestWrapper)
  }

}