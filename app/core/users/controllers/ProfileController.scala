package core.users.controllers

import commons.models.Username
import commons.repositories.ActionRunner
import core.authentication.api.{AuthenticatedActionBuilder, OptionallyAuthenticatedActionBuilder}
import core.commons.controllers.RealWorldAbstractController
import core.users.models._
import core.users.services.ProfileService
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
    actionRunner.runInTransaction(profileService.unfollow(username, currentUserEmail))
      .map(ProfileWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def follow(username: Username): Action[_] = authenticatedAction.async { request =>
    require(username != null)

    val currentUserEmail = request.user.email
    actionRunner.runInTransaction(profileService.follow(username, currentUserEmail))
      .map(ProfileWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def byUsername(username: Username): Action[_] = optionallyAuthenticatedActionBuilder.async { request =>
    require(username != null)

    val maybeEmail = request.user.map(_.email)
    actionRunner.runInTransaction(profileService.byUsername(username, maybeEmail))
      .map(ProfileWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

}