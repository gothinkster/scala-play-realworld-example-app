package core.articles.controllers

import commons.repositories.ActionRunner
import core.articles.exceptions.MissingArticleException
import core.articles.models._
import core.articles.services.ArticleService
import core.authentication.api.{AuthenticatedActionBuilder, OptionallyAuthenticatedActionBuilder}
import core.commons.controllers.RealWorldAbstractController
import core.users.repositories.UserRepo
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class ArticleController(authenticatedAction: AuthenticatedActionBuilder,
                        optionallyAuthenticatedActionBuilder: OptionallyAuthenticatedActionBuilder,
                        userRepo: UserRepo,
                        actionRunner: ActionRunner,
                        articleService: ArticleService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def favorite(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val currentUserEmail = request.user.email
    actionRunner.runInTransaction(articleService.favorite(slug, currentUserEmail))
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
    actionRunner.runInTransaction(articleService.bySlug(slug, maybeCurrentUserEmail))
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
    actionRunner.runInTransaction(articleService.all(pageRequest, currentUserEmail))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def feed(pageRequest: UserFeedPageRequest): Action[AnyContent] = authenticatedAction.async { request =>
    require(pageRequest != null)

    val currentUserEmail = request.user.email
    actionRunner.runInTransaction(articleService.feed(pageRequest, currentUserEmail))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  def create: Action[_] = authenticatedAction.async(validateJson[NewArticleWrapper]) { request =>
    val article = request.body.article

    val createArticleAction = userRepo.byEmail(request.user.email)
      .flatMap(user => articleService.create(article, user))

    actionRunner.runInTransaction(createArticleAction)
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

}
