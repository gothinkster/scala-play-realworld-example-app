package users.controllers

import commons.services.ActionRunner
import authentication.api._
import commons.controllers.RealWorldAbstractController
import users.models._
import users.services.{UserRegistrationService, UserService}
import play.api.libs.json._
import play.api.mvc._

class UserController(authenticatedAction: AuthenticatedActionBuilder,
                     actionRunner: ActionRunner,
                     userRegistrationService: UserRegistrationService,
                     userService: UserService,
                     jwtAuthenticator: TokenGenerator[SecurityUserIdProfile, JwtToken],
                     components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def update: Action[UpdateUserWrapper] = authenticatedAction.async(validateJson[UpdateUserWrapper]) { request =>
    val email = request.user.email
    actionRunner.runTransactionally(userService.update(email, request.body.user))
      .map(userDetails => UserDetailsWithToken(userDetails, request.user.token))
      .map(UserDetailsWithTokenWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover(handleFailedValidation)
  }

  def getCurrentUser: Action[AnyContent] = authenticatedAction.async { request =>
    val email = request.user.email
    actionRunner.runTransactionally(userService.getUserDetails(email))
      .map(userDetails => UserDetailsWithToken(userDetails, request.user.token))
      .map(UserDetailsWithTokenWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def register: Action[UserRegistrationWrapper] = Action.async(validateJson[UserRegistrationWrapper]) { request =>
    actionRunner.runTransactionally(userRegistrationService.register(request.body.user))
      .map(userAndSecurityUserId => {
        val (user, securityUserId) = userAndSecurityUserId
        val jwtToken: JwtToken = generateToken(securityUserId)
        UserDetailsWithToken(user.email, user.username, user.createdAt, user.updatedAt, user.bio, user.image,
          jwtToken.token)
      })
      .map(UserDetailsWithTokenWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover(handleFailedValidation)
  }

  private def generateToken(securityUserId: SecurityUserId) = {
    val profile = SecurityUserIdProfile(securityUserId)
    val jwtToken = jwtAuthenticator.generate(profile)
    jwtToken
  }

}