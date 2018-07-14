package users

import com.softwaremill.macwire.wire
import commons.config.{WithControllerComponents, WithExecutionContextComponents}
import commons.models.Username
import authentication.AuthenticationComponents
import play.api.routing.Router
import play.api.routing.sird._
import users.controllers.{LoginController, ProfileController, UserController}
import users.repositories.{FollowAssociationRepo, ProfileRepo, UserRepo}
import users.services._

trait UserComponents extends AuthenticationComponents with WithControllerComponents with WithExecutionContextComponents {
  lazy val userController: UserController = wire[UserController]
  lazy val userService: UserService = wire[UserService]
  lazy val userRepo: UserRepo = wire[UserRepo]
  lazy val userRegistrationService: UserRegistrationService = wire[UserRegistrationService]
  lazy val userRegistrationValidator: UserRegistrationValidator = wire[UserRegistrationValidator]
  lazy val userUpdateValidator: UserUpdateValidator = wire[UserUpdateValidator]

  lazy val passwordValidator: PasswordValidator = wire[PasswordValidator]
  lazy val usernameValidator: UsernameValidator = wire[UsernameValidator]
  lazy val emailValidator: EmailValidator = wire[EmailValidator]

  lazy val profileController: ProfileController = wire[ProfileController]
  lazy val profileService: ProfileService = wire[ProfileService]
  lazy val profileRepo: ProfileRepo = wire[ProfileRepo]

  lazy val followAssociationRepo: FollowAssociationRepo = wire[FollowAssociationRepo]

  lazy val loginController: LoginController = wire[LoginController]

  val userRoutes: Router.Routes = {
    case POST(p"/users") =>
      userController.register
    case GET(p"/user") =>
      userController.getCurrentUser
    case POST(p"/users/login") =>
      loginController.login
    case PUT(p"/user") =>
      userController.update
    case GET(p"/profiles/$rawUsername") =>
      profileController.findByUsername(Username(rawUsername))
    case POST(p"/profiles/$rawUsername/follow") =>
      profileController.follow(Username(rawUsername))
    case DELETE(p"/profiles/$rawUsername/follow") =>
      profileController.unfollow(Username(rawUsername))
  }
}