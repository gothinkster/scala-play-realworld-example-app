package core.users.controllers

import commons.models.Username
import commons.repositories.ActionRunner
import core.commons.controllers.RealWorldAbstractController
import core.users.models._
import core.users.services.ProfileService
import play.api.libs.json._
import play.api.mvc._

class ProfileController(actionRunner: ActionRunner,
                        profileService: ProfileService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def byUsername(username: Username): Action[_] = Action.async {
    require(username != null)

    actionRunner.runInTransaction(profileService.byUsername(username))
      .map(ProfileWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

}