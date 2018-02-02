package core.articles.controllers

import commons.repositories.ActionRunner
import core.articles.exceptions.MissingArticleException
import core.articles.models._
import core.articles.services.ArticleService
import core.authentication.api.AuthenticatedActionBuilder
import core.commons.controllers.RealWorldAbstractController
import core.users.repositories.UserRepo
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class ArticleController(authenticatedAction: AuthenticatedActionBuilder,
                        userRepo: UserRepo,
                        actionRunner: ActionRunner,
                        articleService: ArticleService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def bySlug(slug: String): Action[AnyContent] = Action.async {
    require(StringUtils.isNotBlank(slug))

    actionRunner.runInTransaction(articleService.bySlug(slug))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingArticleException => NotFound
      })
  }

  def all(pageRequest: MainFeedPageRequest): Action[AnyContent] = Action.async {
    require(pageRequest != null)

    actionRunner.runInTransaction(articleService.all(pageRequest))
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
