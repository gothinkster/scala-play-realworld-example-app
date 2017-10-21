package core.users.controllers

import commons.exceptions.ValidationException
import commons.repositories.ActionRunner
import core.authentication.api.{JwtToken, RealWorldAuthenticator, UsernameProfile}
import core.commons.controllers.RealWorldAbstractController
import core.commons.models.ValidationResultWrapper
import core.users.models.{RegisteredUser, RegisteredUserWrapper, UserRegistrationWrapper}
import core.users.repositories.UserRepo
import core.users.services.UserRegistrationService
import play.api.libs.json._
import play.api.mvc._

class UserController(actionRunner: ActionRunner,
                     userRepo: UserRepo,
                     userRegistrationService: UserRegistrationService,
                     jwtAuthenticator: RealWorldAuthenticator[UsernameProfile, JwtToken],
                     components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def register: Action[UserRegistrationWrapper] = Action.async(validateJson[UserRegistrationWrapper]) { request =>
    val action = userRegistrationService.register(request.body.user)
      .map(user => {
        val profile = new UsernameProfile(user.username)
        val jwtToken = jwtAuthenticator.authenticate(profile)

        RegisteredUser(user.email, user.username, user.createdAt, user.updatedAt, jwtToken.token)
      })
      .map(RegisteredUserWrapper)
      .map(Json.toJson(_))
      .map(Ok(_))

    actionRunner.runInTransaction(action)
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