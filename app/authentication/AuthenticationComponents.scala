package authentication

import authentication.controllers.AuthenticationController
import authentication.pac4j.config.Pac4jComponents
import authentication.repositories.SecurityUserRepo
import authentication.services.{PasswordValidatorImpl, SecurityUserService}
import com.softwaremill.macwire.wire
import commons.CommonsComponents
import commons.config.{WithControllerComponents, WithExecutionContext}
import core.authentication.api.{PasswordValidator, SecurityUserCreator, SecurityUserProvider}
import play.api.routing.Router
import play.api.routing.sird._

trait AuthenticationComponents extends CommonsComponents
  with WithControllerComponents
  with WithExecutionContext
  with Pac4jComponents {

  lazy val passwordValidator: PasswordValidator = wire[PasswordValidatorImpl]
  lazy val securityUserCreator: SecurityUserCreator = wire[SecurityUserService]
  lazy val securityUserProvider: SecurityUserProvider = wire[SecurityUserService]
  lazy val securityUserRepo: SecurityUserRepo = wire[SecurityUserRepo]

  lazy val authenticationController: AuthenticationController = new AuthenticationController(actionRunner, sessionStore,
    usernamePasswordAuthenticator, controllerComponents, dateTimeProvider, jwtGenerator)

  val authenticationRoutes: Router.Routes = {
    case GET(p"/authenticate") => authenticationController.authenticate
  }
}