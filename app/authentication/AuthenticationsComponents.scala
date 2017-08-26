package authentication

import authentication.deadbolt.config.{DeadboltHandlerCache, OAuthDeadboltHandler}
import authentication.oauth2.{AccessTokenRepo, AuthenticationConfig, OAuth2Controller, PlayWithFoodAuthorizationHandler}
import authentication.repositories.SecurityUserRepo
import authentication.services.api.{PasswordValidator, SecurityUserCreator, SecurityUserProvider}
import authentication.services.{PasswordValidatorImpl, SecurityUserService}
import be.objectify.deadbolt.scala.cache._
import be.objectify.deadbolt.scala.{ActionBuilders, DeadboltComponents}
import com.softwaremill.macwire.wire
import commons.CommonsComponents
import commons.config.WithControllerComponents
import play.api.mvc.PlayBodyParsers
import play.api.routing.Router
import play.api.routing.sird._

trait AuthenticationsComponents extends CommonsComponents with DeadboltComponents with WithControllerComponents {
  lazy val passwordValidator: PasswordValidator = wire[PasswordValidatorImpl]
  lazy val securityUserCreator: SecurityUserCreator = wire[SecurityUserService]
  lazy val securityUserProvider: SecurityUserProvider = wire[SecurityUserService]
  lazy val securityUserRepo: SecurityUserRepo = wire[SecurityUserRepo]

  lazy val oAuth2Controller: OAuth2Controller = wire[OAuth2Controller]

  lazy val playWithFoodAuthorizationHandler: PlayWithFoodAuthorizationHandler = wire[PlayWithFoodAuthorizationHandler]
  lazy val authenticationConfig: AuthenticationConfig = wire[AuthenticationConfig]
  lazy val accessTokenRepo: AccessTokenRepo = wire[AccessTokenRepo]


  override lazy val patternCache: PatternCache = wire[DefaultPatternCache]
  override lazy val compositeCache: CompositeCache = wire[DefaultCompositeCache]
  override lazy val handlers: HandlerCache = wire[DeadboltHandlerCache]
  lazy val oAuthDeadboltHandler: OAuthDeadboltHandler = wire[OAuthDeadboltHandler]

  protected def createActionBuilders: PlayBodyParsers => ActionBuilders = parsers => actionBuilders(deadboltActions(parsers))

  val authenticationRoutes: Router.Routes = {
    case POST(p"/oauth/accessToken") => oAuth2Controller.accessToken
  }
}