package articles.controllers

import commons.exceptions.MissingModelException
import commons.services.ActionRunner
import articles.exceptions.AuthorMismatchException
import articles.models._
import articles.services.{ArticleReadService, ArticleWriteService}
import commons.controllers.RealWorldAbstractController
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import users.controllers.{AuthenticatedActionBuilder, OptionallyAuthenticatedActionBuilder}

class ArticleController(authenticatedAction: AuthenticatedActionBuilder,
                        optionallyAuthenticatedActionBuilder: OptionallyAuthenticatedActionBuilder,
                        actionRunner: ActionRunner,
                        articleWriteService: ArticleWriteService,
                        articleReadService: ArticleReadService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def unfavorite(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val userId = request.user.userId
    actionRunner.runTransactionally(articleWriteService.unfavorite(slug, userId))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def favorite(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val userId = request.user.userId
    actionRunner.runTransactionally(articleWriteService.favorite(slug, userId))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def findBySlug(slug: String): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    require(StringUtils.isNotBlank(slug))

    val maybeUserId = request.authenticatedUserOption.map(_.userId)
    actionRunner.runTransactionally(articleReadService.findBySlug(slug, maybeUserId))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def findAll(pageRequest: MainFeedPageRequest): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    require(pageRequest != null)

    val maybeUserId = request.authenticatedUserOption.map(_.userId)
    actionRunner.runTransactionally(articleReadService.findAll(pageRequest, maybeUserId))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def findFeed(pageRequest: UserFeedPageRequest): Action[AnyContent] = authenticatedAction.async { request =>
    require(pageRequest != null)

    val userId = request.user.userId
    actionRunner.runTransactionally(articleReadService.findFeed(pageRequest, userId))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def create: Action[NewArticleWrapper] = authenticatedAction.async(validateJson[NewArticleWrapper]) { request =>
    val article = request.body.article
    val userId = request.user.userId
    actionRunner.runTransactionally(articleWriteService.create(article, userId))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover(handleFailedValidation)
  }

  def update(slug: String): Action[ArticleUpdateWrapper] = {
    authenticatedAction.async(validateJson[ArticleUpdateWrapper]) { request =>
      val articleUpdate = request.body.article
      val userId = request.user.userId
      actionRunner.runTransactionally(articleWriteService.update(slug, articleUpdate, userId))
        .map(ArticleWrapper(_))
        .map(Json.toJson(_))
        .map(Ok(_))
        .recover(handleFailedValidation.orElse({
          case _: MissingModelException => NotFound
        }))
    }
  }

  def delete(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val userId = request.user.userId
    actionRunner.runTransactionally(articleWriteService.delete(slug, userId))
      .map(_ => Ok)
      .recover({
        case _: AuthorMismatchException => Forbidden
        case _: MissingModelException => NotFound
      })
  }

}
