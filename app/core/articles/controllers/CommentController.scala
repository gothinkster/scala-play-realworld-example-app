package core.articles.controllers

import commons.repositories.ActionRunner
import core.articles.exceptions.{AuthorMismatchException, MissingArticleException, MissingCommentException}
import core.articles.models._
import core.articles.services.CommentService
import core.authentication.api.AuthenticatedActionBuilder
import core.commons.controllers.RealWorldAbstractController
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class CommentController(authenticatedAction: AuthenticatedActionBuilder,
                        actionRunner: ActionRunner,
                        commentService: CommentService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def delete(id: CommentId): Action[AnyContent] = authenticatedAction.async { request =>

    actionRunner.runInTransaction(commentService.delete(id, request.user.email))
      .map(_ => Ok)
      .recover({
        case _: AuthorMismatchException =>
          Forbidden
        case _: MissingArticleException =>
          NotFound
        case _: MissingCommentException =>
          NotFound
      })
  }

  def byArticleSlug(slug: String): Action[AnyContent] = Action.async {
    require(StringUtils.isNotBlank(slug))

    actionRunner.runInTransaction(commentService.byArticleSlug(slug))
      .map(CommentList(_))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

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
