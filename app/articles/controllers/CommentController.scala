package articles.controllers

import commons.exceptions.MissingModelException
import commons.services.ActionRunner
import articles.exceptions.AuthorMismatchException
import articles.models._
import articles.services.CommentService
import commons.controllers.RealWorldAbstractController
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import users.controllers.{AuthenticatedActionBuilder, OptionallyAuthenticatedActionBuilder}

class CommentController(authenticatedAction: AuthenticatedActionBuilder,
                        optionallyAuthenticatedActionBuilder: OptionallyAuthenticatedActionBuilder,
                        actionRunner: ActionRunner,
                        commentService: CommentService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def delete(id: CommentId): Action[AnyContent] = authenticatedAction.async { request =>

    actionRunner.runTransactionally(commentService.delete(id, request.user.userId))
      .map(_ => Ok)
      .recover({
        case _: AuthorMismatchException => Forbidden
        case _: MissingModelException => NotFound
      })
  }

  def findByArticleSlug(slug: String): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    require(StringUtils.isNotBlank(slug))

    val maybeUserId = request.authenticatedUserOption.map(_.userId)
    actionRunner.runTransactionally(commentService.findByArticleSlug(slug, maybeUserId))
      .map(CommentList(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def create(slug: String): Action[_] = authenticatedAction.async(validateJson[NewCommentWrapper]) { request =>
    require(StringUtils.isNotBlank(slug))

    val newComment = request.body.comment
    val userId = request.user.userId

    actionRunner.runTransactionally(commentService.create(newComment, slug, userId)
      .map(CommentWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_)))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

}
