package articles.controllers

import articles.models.Article
import articles.services.ArticleService
import commons.repositories.ActionRunner
import play.api.libs.json.{Format, JsArray, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.ExecutionContext
import commons.models._

case class PageRequest(limit: Long, offset: Long, orderings: List[Ordering])
case class Page[Model](models: Seq[Model], count: Long)

case class ArticlePage(articles: Seq[Article], articlesCount: Long)

class ArticlePageJsonMappings

object ArticlePageJsonMappings {
  import mappings.ArticleJsonMappings._

  implicit val articlePageFormat: Format[ArticlePage] = Json.format[ArticlePage]
}

class ArticleController(actionRunner: ActionRunner,
                        articleService: ArticleService,
                        components: ControllerComponents,
                        implicit private val ec: ExecutionContext) extends AbstractController(components) {

  import mappings.ArticleJsonMappings._
  import ArticlePageJsonMappings._

  def all(pageRequest: PageRequest): Action[AnyContent] = {
    Action.async {
      actionRunner.runInTransaction(articleService.all(pageRequest))
        .map(page => ArticlePage(page.models, page.count))
        .map(Json.toJson(_))
        .map(Ok(_))
    }
  }

}
