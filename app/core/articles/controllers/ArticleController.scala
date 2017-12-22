package core.articles.controllers

import commons.models._
import commons.repositories.ActionRunner
import core.articles.models._
import core.articles.services.ArticleService
import core.authentication.api.AuthenticatedActionBuilder
import core.commons.controllers.RealWorldAbstractController
import core.users.repositories.UserRepo
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class ArticleController(authenticatedAction: AuthenticatedActionBuilder,
                        userRepo: UserRepo,
                        actionRunner: ActionRunner,
                        articleService: ArticleService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def all(pageRequest: PageRequest): Action[AnyContent] = Action.async {
    actionRunner.runInTransaction(articleService.all(pageRequest))
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
