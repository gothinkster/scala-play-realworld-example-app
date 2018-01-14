package core.users

import authentication.AuthenticationComponents
import com.softwaremill.macwire.wire
import commons.config.{WithControllerComponents, WithExecutionContext}
import commons.models.Username
import play.api.routing.Router
import play.api.routing.sird._
import core.users.controllers.{ProfileController, UserController}
import core.users.repositories.UserRepo
import core.users.services.api.{UserCreator, UserProvider}
import core.users.services._

trait UserComponents extends AuthenticationComponents with WithControllerComponents with WithExecutionContext {
  lazy val userController: UserController = wire[UserController]
  lazy val userService: UserService = wire[UserService]
  lazy val userRepo: UserRepo = wire[UserRepo]
  lazy val userCreator: UserCreator = wire[UserCreatorImpl]
  lazy val userProvider: UserProvider = wire[UserProviderImpl]
  lazy val userRegistrationService: UserRegistrationService = wire[UserRegistrationService]
  lazy val userRegistrationValidator: UserRegistrationValidator = wire[UserRegistrationValidator]
  lazy val userUpdateValidator: UserUpdateValidator = wire[UserUpdateValidator]

  lazy val passwordValidator: PasswordValidator = wire[PasswordValidator]
  lazy val usernameValidator: UsernameValidator = wire[UsernameValidator]
  lazy val emailValidator: EmailValidator = wire[EmailValidator]

  lazy val profileController: ProfileController = wire[ProfileController]
  lazy val profileService: ProfileService = wire[ProfileService]

  val userRoutes: Router.Routes = {
    case POST(p"/users") =>
      userController.register
    case GET(p"/user") =>
      userController.getCurrentUser
    case PUT(p"/user") =>
      userController.update
    case GET(p"/profiles/$rawUsername") =>
      profileController.byUsername(Username(rawUsername))
  }
}