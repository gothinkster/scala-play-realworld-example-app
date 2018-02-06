package core.articles.controllers

import commons.repositories.ActionRunner
import core.articles.exceptions.{AuthorMismatchException, MissingArticleException, MissingCommentException}
import core.articles.models._
import core.articles.services.CommentService
import core.authentication.api.{AuthenticatedActionBuilder, OptionallyAuthenticatedActionBuilder}
import core.commons.controllers.RealWorldAbstractController
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class CommentController(authenticatedAction: AuthenticatedActionBuilder,
                        optionallyAuthenticatedActionBuilder: OptionallyAuthenticatedActionBuilder,
                        actionRunner: ActionRunner,
                        commentService: CommentService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def delete(id: CommentId): Action[AnyContent] = authenticatedAction.async { request =>

    actionRunner.runInTransaction(commentService.delete(id, request.user.email))
      .map(_ => Ok)
      .recover({
        case _: AuthorMismatchException => Forbidden
        case _: MissingArticleException | _: MissingCommentException => NotFound
      })
  }

  def byArticleSlug(slug: String): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    require(StringUtils.isNotBlank(slug))

    val maybeCurrentUserEmail = request.user.map(_.email)
    actionRunner.runInTransaction(commentService.byArticleSlug(slug, maybeCurrentUserEmail))
      .map(CommentList(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingArticleException => NotFound
      })
  }

  def create(slug: String): Action[_] = authenticatedAction.async(validateJson[NewCommentWrapper]) { request =>
    require(StringUtils.isNotBlank(slug))

    val newComment = request.body.comment
    val email = request.user.email

    actionRunner.runInTransaction(commentService.create(newComment, slug, email)
      .map(CommentWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_)))
      .recover({
        case _: MissingArticleException => NotFound
      })
  }

}
