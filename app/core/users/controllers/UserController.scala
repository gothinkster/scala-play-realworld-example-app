package core.users.controllers

import commons.exceptions.ValidationException
import commons.models.Email
import commons.repositories.ActionRunner
import core.authentication.api.{AuthenticatedActionBuilder, EmailProfile, JwtToken, RealWorldAuthenticator}
import core.commons.controllers.RealWorldAbstractController
import core.commons.models.ValidationResultWrapper
import core.users.models._
import core.users.repositories.UserRepo
import core.users.services.{UserRegistrationService, UserService}
import play.api.libs.json._
import play.api.mvc._

class UserController(authenticatedAction: AuthenticatedActionBuilder,
                     actionRunner: ActionRunner,
                     userRepo: UserRepo,
                     userRegistrationService: UserRegistrationService,
                     userService: UserService,
                     jwtAuthenticator: RealWorldAuthenticator[EmailProfile, JwtToken],
                     components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def update: Action[UpdateUserWrapper] = authenticatedAction.async(validateJson[UpdateUserWrapper]) { request =>
    val email = request.user.email

    actionRunner.runTransactionally(userService.update(email, request.body.user))
      .map(userDetails => {
        val jwtTokenWithNewEmail: JwtToken = generateToken(userDetails.email)
        UserDetailsWithToken(userDetails, jwtTokenWithNewEmail.token)
      })
      .map(UserDetailsWithTokenWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover(handleFailedValidation)
  }

  def getCurrentUser: Action[AnyContent] = authenticatedAction.async { request =>
    val email = request.user.email
    actionRunner.runTransactionally(userService.getUserDetails(email))
      .map(userDetails => {
        val jwtTokenWithNewEmail: JwtToken = generateToken(userDetails.email)
        UserDetailsWithToken(userDetails, jwtTokenWithNewEmail.token)
      })
      .map(UserDetailsWithTokenWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def register: Action[UserRegistrationWrapper] = Action.async(validateJson[UserRegistrationWrapper]) { request =>
    actionRunner.runTransactionally(userRegistrationService.register(request.body.user))
      .map(user => {
        val jwtToken: JwtToken = generateToken(user.email)
        UserDetailsWithToken(user.email, user.username, user.createdAt, user.updatedAt, user.bio, user.image,
          jwtToken.token)
      })
      .map(UserDetailsWithTokenWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover(handleFailedValidation)
  }

  private def generateToken(email: Email) = {
    val profile = new EmailProfile(email.value)
    val jwtToken = jwtAuthenticator.authenticate(profile)
    jwtToken
  }

  private def handleFailedValidation: PartialFunction[Throwable, Result] = {
    case e: ValidationException =>
      val errors = e.violations
        .groupBy(_.property)
        .mapValues(_.map(propertyViolation => propertyViolation.violation.message))

      val wrapper: ValidationResultWrapper = ValidationResultWrapper(errors)
      UnprocessableEntity(Json.toJson(wrapper))
  }
}