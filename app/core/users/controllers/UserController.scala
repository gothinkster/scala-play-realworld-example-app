package core.users.controllers

import commons.exceptions.ValidationException
import commons.models.Login
import commons.repositories.ActionRunner
import core.commons.controllers.RealWorldAbstractController
import core.commons.models.ValidationResultWrapper
import core.users.models.UserRegistrationWrapper
import core.users.repositories.UserRepo
import core.users.services.UserRegistrationService
import play.api.libs.json._
import play.api.mvc._

class UserController(actionRunner: ActionRunner,
                     userRepo: UserRepo,
                     userRegistrationService: UserRegistrationService,
                     components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def all: Action[AnyContent] = Action.async {
    val action = userRepo.all
      .map(Json.toJson(_))
      .map(Ok(_))

    actionRunner.runInTransaction(action)
  }

  def byLogin(login: String): Action[AnyContent] = Action.async {
    val action = userRepo.byLogin(Login(login))
      .map(Json.toJson(_))
      .map(Ok(_))

    actionRunner.runInTransaction(action)
  }

  def register: Action[UserRegistrationWrapper] = Action.async(validateJson[UserRegistrationWrapper]) { request =>
    val action = userRegistrationService.register(request.body.user)
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