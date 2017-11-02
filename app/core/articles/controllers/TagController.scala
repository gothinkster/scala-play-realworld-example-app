package core.articles.controllers

import commons.repositories.ActionRunner
import core.articles.models._
import core.articles.services.TagService
import core.commons.controllers.RealWorldAbstractController
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class TagController(actionRunner: ActionRunner,
                    tagService: TagService,
                    components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def all: Action[AnyContent] = Action.async {
    val allAction = tagService.all
      .map(tags => tags.map(_.name))
      .map(TagListWrapper)
      .map(Json.toJson(_))
      .map(Ok(_))

    actionRunner.runInTransaction(allAction)
  }

}