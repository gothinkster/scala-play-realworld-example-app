package users

import authentication.AuthenticationsComponents
import com.softwaremill.macwire.wire
import commons.config.WithControllerComponents
import play.api.routing.Router
import play.api.routing.sird._
import users.controllers.UserController
import users.repositories.UserRepo
import users.services.api.{UserCreator, UserProvider}
import users.services.{UserCreatorImpl, UserProviderImpl, UserRegistrationService, UserRegistrationValidator}

trait UsersComponents extends AuthenticationsComponents with WithControllerComponents {
  lazy val userController: UserController = wire[UserController]
  lazy val userRepo: UserRepo = wire[UserRepo]
  lazy val userCreator: UserCreator = wire[UserCreatorImpl]
  lazy val userProvider: UserProvider = wire[UserProviderImpl]
  lazy val userRegistrationService: UserRegistrationService = wire[UserRegistrationService]
  lazy val userRegistrationValidator: UserRegistrationValidator = wire[UserRegistrationValidator]

  val userRoutes: Router.Routes = {
    case GET(p"/users") => userController.all
    case GET(p"/users/login/$login") => userController.byLogin(login)
    case POST(p"/users/register") => userController.register
  }
}