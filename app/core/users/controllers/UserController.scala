package core.users.controllers

import commons.exceptions.ValidationException
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

    actionRunner.runInTransaction(userService.update(email, request.body.user))
      .map(userDetails => UserDetailsWrapper(userDetails))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover(handleFailedValidation)
  }

  def getCurrentUser: Action[AnyContent] = authenticatedAction.async { request =>
    val email = request.user.email

    actionRunner.runInTransaction(userRepo.byEmail(email))
      .map(user => UserDetailsWrapper(UserDetails(user)))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def register: Action[UserRegistrationWrapper] = Action.async(validateJson[UserRegistrationWrapper]) { request =>
    actionRunner.runInTransaction(userRegistrationService.register(request.body.user))
      .map(user => {
        val profile = new EmailProfile(user.email.value)
        val jwtToken = jwtAuthenticator.authenticate(profile)

        RegisteredUser(user.email, user.username, user.createdAt, user.updatedAt, jwtToken.token)
      })
      .map(RegisteredUserWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover(handleFailedValidation)
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