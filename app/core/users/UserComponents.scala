package core.users

import authentication.AuthenticationComponents
import com.softwaremill.macwire.wire
import commons.config.{WithControllerComponents, WithExecutionContext}
import play.api.routing.Router
import play.api.routing.sird._
import core.users.controllers.UserController
import core.users.repositories.UserRepo
import core.users.services.api.{UserCreator, UserProvider}
import core.users.services.{UserCreatorImpl, UserProviderImpl, UserRegistrationService, UserRegistrationValidator}

trait UserComponents extends AuthenticationComponents with WithControllerComponents with WithExecutionContext {
  lazy val userController: UserController = wire[UserController]
  lazy val userRepo: UserRepo = wire[UserRepo]
  lazy val userCreator: UserCreator = wire[UserCreatorImpl]
  lazy val userProvider: UserProvider = wire[UserProviderImpl]
  lazy val userRegistrationService: UserRegistrationService = wire[UserRegistrationService]
  lazy val userRegistrationValidator: UserRegistrationValidator = wire[UserRegistrationValidator]

  val userRoutes: Router.Routes = {
    case GET(p"/core.users") => userController.all
    case GET(p"/core.users/login/$login") => userController.byLogin(login)
    case POST(p"/core.users/register") => userController.register
  }
}