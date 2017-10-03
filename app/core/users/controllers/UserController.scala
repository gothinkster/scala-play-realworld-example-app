package core.users.controllers

import commons.exceptions.ValidationException
import commons.models.Login
import commons.repositories.ActionRunner
import core.commons.controllers.RealWorldAbstractController
import core.users.models.UserRegistration
import core.users.repositories.UserRepo
import core.users.services.UserRegistrationService
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future

class UserController(actionRunner: ActionRunner,
                               userRepo: UserRepo,
                               userRegistrationService: UserRegistrationService,
                               components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def all: Action[AnyContent] =
    Action.async {
      actionRunner.runInTransaction(userRepo.all)
        .map(Json.toJson(_))
        .map(Ok(_))
    }

  def byLogin(login: String): Action[AnyContent] = Action.async {
    actionRunner.runInTransaction(userRepo.byLogin(Login(login)))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def register: Action[_] = Action.async(validateJson[UserRegistration]) { request =>
    try {
      doRegister(request.body)
    } catch {
      case e: ValidationException => Future.successful(BadRequest(Json.toJson(e.violatedConstraints.toString)))
    }
  }

  private def doRegister(userRegistration: UserRegistration) = {
    actionRunner.runInTransaction(userRegistrationService.register(userRegistration))
      .map(Json.toJson(_))
      .map(Ok(_))
  }
}