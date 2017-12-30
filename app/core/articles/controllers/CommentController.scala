package core.articles.controllers

import commons.repositories.ActionRunner
import core.articles.models._
import core.articles.services.{ArticleService, CommentService}
import core.authentication.api.AuthenticatedActionBuilder
import core.commons.controllers.RealWorldAbstractController
import core.users.repositories.UserRepo
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, ControllerComponents}

class CommentController(authenticatedAction: AuthenticatedActionBuilder,
                        userRepo: UserRepo,
                        actionRunner: ActionRunner,
                        articleService: ArticleService,
                        commentService: CommentService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def create(slug: String): Action[_] = authenticatedAction.async(validateJson[NewCommentWrapper]) { request =>
    require(StringUtils.isNotBlank(slug))

    val newComment = request.body.comment
    val email = request.user.email

    val action = commentService.create(newComment, slug, email)
      .map(CommentWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))

    actionRunner.runInTransaction(action)
  }

}
