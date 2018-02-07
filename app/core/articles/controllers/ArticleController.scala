package core.articles.controllers

import commons.repositories.ActionRunner
import core.articles.exceptions.{AuthorMismatchException, MissingArticleException}
import core.articles.models._
import core.articles.services.ArticleService
import core.authentication.api.{AuthenticatedActionBuilder, OptionallyAuthenticatedActionBuilder}
import core.commons.controllers.RealWorldAbstractController
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class ArticleController(authenticatedAction: AuthenticatedActionBuilder,
                        optionallyAuthenticatedActionBuilder: OptionallyAuthenticatedActionBuilder,
                        actionRunner: ActionRunner,
                        articleService: ArticleService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def unfavorite(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleService.unfavorite(slug, currentUserEmail))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingArticleException => NotFound
      })
  }

  def favorite(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleService.favorite(slug, currentUserEmail))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingArticleException => NotFound
      })
  }

  def bySlug(slug: String): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    require(StringUtils.isNotBlank(slug))

    val maybeCurrentUserEmail = request.user.map(_.email)
    actionRunner.runTransactionally(articleService.bySlug(slug, maybeCurrentUserEmail))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingArticleException => NotFound
      })
  }

  def all(pageRequest: MainFeedPageRequest): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    require(pageRequest != null)

    val currentUserEmail = request.user.map(_.email)
    actionRunner.runTransactionally(articleService.all(pageRequest, currentUserEmail))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def feed(pageRequest: UserFeedPageRequest): Action[AnyContent] = authenticatedAction.async { request =>
    require(pageRequest != null)

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleService.feed(pageRequest, currentUserEmail))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def create: Action[NewArticleWrapper] = authenticatedAction.async(validateJson[NewArticleWrapper]) { request =>
    val article = request.body.article
    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleService.create(article, currentUserEmail))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def update(slug: String): Action[ArticleUpdateWrapper] = {
    authenticatedAction.async(validateJson[ArticleUpdateWrapper]) { request =>
      val articleUpdate = request.body.article
      val currentUserEmail = request.user.email
      actionRunner.runTransactionally(articleService.update(slug, articleUpdate, currentUserEmail))
        .map(ArticleWrapper(_))
        .map(Json.toJson(_))
        .map(Ok(_))
        .recover({
          case _: MissingArticleException => NotFound
        })
    }
  }

  def delete(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val currentUserEmail = request.user.email
    actionRunner.runTransactionally(articleService.delete(slug, currentUserEmail))
      .map(_ => Ok)
      .recover({
        case _: AuthorMismatchException => Forbidden
        case _: MissingArticleException => NotFound
      })
  }

}
