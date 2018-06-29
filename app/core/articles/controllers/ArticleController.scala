package core.articles.controllers

import commons.exceptions.MissingModelException
import commons.services.ActionRunner
import core.articles.exceptions.AuthorMismatchException
import core.articles.models._
import core.articles.services.{ArticleReadService, ArticleWriteService}
import core.authentication.api.{AuthenticatedActionBuilder, OptionallyAuthenticatedActionBuilder}
import core.commons.controllers.RealWorldAbstractController
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class ArticleController(authenticatedAction: AuthenticatedActionBuilder,
                        optionallyAuthenticatedActionBuilder: OptionallyAuthenticatedActionBuilder,
                        actionRunner: ActionRunner,
                        articleWriteService: ArticleWriteService,
                        articleReadService: ArticleReadService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def unfavorite(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleWriteService.unfavorite(slug, currentUserEmail))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def favorite(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleWriteService.favorite(slug, currentUserEmail))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def findBySlug(slug: String): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    require(StringUtils.isNotBlank(slug))

    val maybeCurrentUserEmail = request.authenticatedUserOption.map(_.email)
    actionRunner.runTransactionally(articleReadService.findBySlug(slug, maybeCurrentUserEmail))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def findAll(pageRequest: MainFeedPageRequest): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    require(pageRequest != null)

    val currentUserEmail = request.authenticatedUserOption.map(_.email)
    actionRunner.runTransactionally(articleReadService.findAll(pageRequest, currentUserEmail))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def findFeed(pageRequest: UserFeedPageRequest): Action[AnyContent] = authenticatedAction.async { request =>
    require(pageRequest != null)

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleReadService.findFeed(pageRequest, currentUserEmail))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def create: Action[NewArticleWrapper] = authenticatedAction.async(validateJson[NewArticleWrapper]) { request =>
    val article = request.body.article
    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleWriteService.create(article, currentUserEmail))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover(handleFailedValidation)
  }

  def update(slug: String): Action[ArticleUpdateWrapper] = {
    authenticatedAction.async(validateJson[ArticleUpdateWrapper]) { request =>
      val articleUpdate = request.body.article
      val currentUserEmail = request.user.email
      actionRunner.runTransactionally(articleWriteService.update(slug, articleUpdate, currentUserEmail))
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

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleWriteService.delete(slug, currentUserEmail))
      .map(_ => Ok)
      .recover({
        case _: AuthorMismatchException => Forbidden
        case _: MissingModelException => NotFound
      })
  }

}
