package authentication.oauth2

import java.util.UUID
import javax.inject.Inject

import authentication.models.SecurityUser
import authentication.oauth2.exceptions.{MissingAccessTokenException, MissingSecurityUserException}
import authentication.repositories.SecurityUserRepo
import commons.models.Login
import commons.repositories.{ActionRunner, DateTimeProvider}
import commons.utils.DateUtils
import commons.utils.DbioUtils._
import org.mindrot.jbcrypt.BCrypt
import play.api.Logger
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scalaoauth2.provider.{AccessToken => ScalaOauth2AccessToken, _}

class PlayWithFoodAuthorizationHandler(authConfig: AuthenticationConfig,
                                                 dateTimeProvider: DateTimeProvider,
                                                 accessTokenRepo: AccessTokenRepo,
                                                 securityUserRepository: SecurityUserRepo,
                                                 actionRunner: ActionRunner)
  extends DataHandler[SecurityUser] {

  private def containsUserCredentials(request: AuthorizationRequest) = {
    request.param(authConfig.usernameParamName).isDefined &&
      request.param(authConfig.passwordParamName).isDefined
  }

  override def validateClient(credential: Option[ClientCredential], request: AuthorizationRequest): Future[Boolean] = {
    Future.successful(
      // no support for client secret
      credential.exists(c => authConfig.clientId.equals(c.clientId)) && containsUserCredentials(request)
    )
  }

  def authenticate(givenPassword: String, secUsr: SecurityUser): Boolean = {
    val authenticated = BCrypt.checkpw(givenPassword, secUsr.password.value)
    if (!authenticated) Logger.debug(s"Password mismatch: $secUsr")
    authenticated
  }

  override def findUser(credential: Option[ClientCredential],
                        request: AuthorizationRequest): Future[Option[SecurityUser]] = {
    val (login, password) = getUserCredentials(request)

    val action: DBIO[Option[SecurityUser]] = securityUserRepository.byLogin(Login(login))
      .map(maybeSecUser => {
        if (maybeSecUser.isEmpty) Logger.warn(s"Missing security user with login: $login")
        maybeSecUser
      }).map(maybeSecUser => {
      maybeSecUser.filter((secUsr: SecurityUser) => authenticate(password, secUsr))
    })
    actionRunner.runInTransaction(action)
  }

  private def getUserCredentials(request: AuthorizationRequest) = {
    (request.param(authConfig.usernameParamName).get,
      request.param(authConfig.passwordParamName).get)
  }

  override def createAccessToken(authInfo: AuthInfo[SecurityUser]): Future[ScalaOauth2AccessToken] = {
    val playWithFoodAccessToken = AccessToken(AccessTokenId(-1),
      authInfo.user.id,
      generateToken,
      null,
      null
    )

    val action = accessTokenRepo.create(playWithFoodAccessToken)
      .map(AccessTokenToScalaOAuth2AccessTokenMapper.map)

    actionRunner.runInTransaction(action)
  }

  private def generateToken = UUID.randomUUID().toString

  override def getStoredAccessToken(authInfo: AuthInfo[SecurityUser]): Future[Option[ScalaOauth2AccessToken]] = {
    def toAccessTokenIfExists(maybeToken: Option[AccessToken]): Option[ScalaOauth2AccessToken] = {
      maybeToken match {
        case token: Some[_] => token.map(AccessTokenToScalaOAuth2AccessTokenMapper.map)
        case None =>
          None
      }
    }

    val id = authInfo.user.id
    val action = accessTokenRepo.bySecurityUserId(id)
      .map(toAccessTokenIfExists)

    actionRunner.runInTransaction(action)
  }

  override def refreshAccessToken(authInfo: AuthInfo[SecurityUser], refreshToken: String): Future[ScalaOauth2AccessToken] = ???

  override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[SecurityUser]]] = ???

  override def deleteAuthCode(code: String): Future[Unit] = ???

  override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[SecurityUser]]] = ???

  override def findAuthInfoByAccessToken(ScalaOauth2AccessToken: ScalaOauth2AccessToken): Future[Option[AuthInfo[SecurityUser]]] = {
    val token = ScalaOauth2AccessToken.token

    val action: DBIO[Option[AuthInfo[SecurityUser]]] = accessTokenRepo.byToken(token)
      .flatMap(optionToDbio(_, new MissingAccessTokenException(token)))
      .flatMap(token => securityUserRepository.byId(token.securityUserId))
      .flatMap(optionToDbio(_, new MissingSecurityUserException(token)))
      .map(securityUser =>
        Some(AuthInfo(
          securityUser,
          Some(authConfig.clientId),
          Some(authConfig.scope),
          Some(authConfig.redirectUri)
        ))
      )
    actionRunner.runInTransaction(action)
  }

  override def findAccessToken(token: String): Future[Option[ScalaOauth2AccessToken]] = {
    val maybeAccessTokenAction = accessTokenRepo.byToken(token)
      .map {
        case accessToken: Some[_] => accessToken.map(AccessTokenToScalaOAuth2AccessTokenMapper.map)
        case None =>
          Logger.warn(s"Request's token ($token) not found")
          None
      }

    actionRunner.runInTransaction(maybeAccessTokenAction)
  }

  object AccessTokenToScalaOAuth2AccessTokenMapper {
    def map(accessToken: AccessToken): ScalaOauth2AccessToken = ScalaOauth2AccessToken(
      accessToken.token,
      None,
      Some(authConfig.scope),
      Some(authConfig.accessTokenLifeDuration.getSeconds),
      DateUtils.toOldJavaDate(accessToken.createdAt)
    )
  }

}

