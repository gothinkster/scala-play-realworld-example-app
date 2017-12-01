package core.articles.controllers

import commons.models._
import commons.repositories.ActionRunner
import core.articles.models._
import core.articles.services.ArticleService
import core.authentication.api.AuthenticatedActionBuilder
import core.commons.controllers.RealWorldAbstractController
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}

class ArticleController(authenticatedAction: AuthenticatedActionBuilder,
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

    actionRunner.runInTransaction(articleService.create(article))
        .map(ArticleWrapper(_))
        .map(Json.toJson(_))
        .map(Ok(_))
  }

}
