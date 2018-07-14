package articles.controllers

import commons.services.ActionRunner
import articles.models._
import articles.services.TagService
import commons.controllers.RealWorldAbstractController
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class TagController(actionRunner: ActionRunner,
                    tagService: TagService,
                    components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def findAll: Action[AnyContent] = Action.async {
    val allAction = tagService.findAll
      .map(tags => tags.map(_.name))
      .map(TagListWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))

    actionRunner.runTransactionally(allAction)
  }

}