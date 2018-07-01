package users.controllers

import commons.exceptions.MissingModelException
import commons.models.Username
import commons.services.ActionRunner
import authentication.api.{AuthenticatedActionBuilder, OptionallyAuthenticatedActionBuilder}
import commons.controllers.RealWorldAbstractController
import users.models._
import users.services.ProfileService
import play.api.libs.json._
import play.api.mvc._

class ProfileController(authenticatedAction: AuthenticatedActionBuilder,
                        optionallyAuthenticatedActionBuilder: OptionallyAuthenticatedActionBuilder,
                        actionRunner: ActionRunner,
                        profileService: ProfileService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def unfollow(username: Username): Action[_] = authenticatedAction.async { request =>
    require(username != null)

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(profileService.unfollow(username, currentUserEmail))
      .map(ProfileWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def follow(username: Username): Action[_] = authenticatedAction.async { request =>
    require(username != null)

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(profileService.follow(username, currentUserEmail))
      .map(ProfileWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def findByUsername(username: Username): Action[_] = optionallyAuthenticatedActionBuilder.async { request =>
    require(username != null)

    val maybeEmail = request.authenticatedUserOption.map(_.email)
    actionRunner.runTransactionally(profileService.findByUsername(username, maybeEmail))
      .map(ProfileWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

}