package authentication.deadbolt.config

import authentication.models.SecurityUser
import authentication.oauth2.PlayWithFoodAuthorizationHandler
import be.objectify.deadbolt.scala.models.Subject
import be.objectify.deadbolt.scala.{AuthenticatedRequest, DeadboltHandler, DynamicResourceHandler}
import javax.inject.Inject
import play.api.mvc.{Request, Result, Results}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaoauth2.provider.{AuthInfo, OAuth2ProtectedResourceProvider}
import be.objectify.deadbolt.scala.models.{Permission, Role}

private[authentication] class OAuthDeadboltHandler(dataHandler: PlayWithFoodAuthorizationHandler)
  extends DeadboltHandler
    with OAuth2ProtectedResourceProvider {

  val dynamicHandler: Option[DynamicResourceHandler] = Option.empty

  override def beforeAuthCheck[A](request: Request[A]): Future[Option[Result]] = Future(None)

  override def getDynamicResourceHandler[A](request: Request[A]): Future[Option[DynamicResourceHandler]] = Future.successful(dynamicHandler)

  override def getSubject[A](request: AuthenticatedRequest[A]): Future[Option[Subject]] =
    request.subject match {
      case Some(_) => Future.successful(request.subject)
      case _ => protectedResource.handleRequest(request, dataHandler).map {
        case Left(_) => None
        case Right(authInfo: AuthInfo[AccountInfo]) => Some(new OAuthSubject(authInfo))
      }
    }

  override def onAuthFailure[A](request: AuthenticatedRequest[A]): Future[Result] = Future(Results.Unauthorized)
}

private[authentication] case class AccountInfo(username: String)

private[authentication] class OAuthSubject(authInfo: AuthInfo[AccountInfo]) extends Subject {

  val scopes = OAuthScope(authInfo.scope.get)

  override def identifier: String = authInfo.user.username

  override def permissions: List[be.objectify.deadbolt.scala.models.Permission] = scopes

  override def roles: List[be.objectify.deadbolt.scala.models.Role] = scopes
}

private[authentication] class OAuthScope(scope: String) extends Role with Permission {

  override def name: String = scope

  override def value: String = scope

}

private[authentication] object OAuthScope {

  def apply(scopeString: String): List[OAuthScope] = scopeString.split(",").toList.map(s => new OAuthScope(s.trim))

}