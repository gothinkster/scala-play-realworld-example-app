package core.articles

import core.articles.controllers.{ArticleController, TagController}
import core.articles.models.ArticleMetaModel
import core.articles.repositories.{ArticleRepo, TagRepo}
import core.articles.services.{ArticleService, TagService}
import core.authentication.api.AuthenticatedActionBuilder
import com.softwaremill.macwire.wire
import commons.CommonsComponents
import commons.config.{WithControllerComponents, WithExecutionContext}
import commons.models._
import play.api.routing.Router
import play.api.routing.sird._

trait ArticleComponents extends WithControllerComponents with CommonsComponents with WithExecutionContext {
  def authenticatedAction: AuthenticatedActionBuilder
  lazy val articleController: ArticleController = wire[ArticleController]
  protected lazy val articleService: ArticleService = wire[ArticleService]
  protected lazy val articleRepo: ArticleRepo = wire[ArticleRepo]

  val articleRoutes: Router.Routes = {
    case GET(p"/articles" ? q_o"limit=${long(limit)}" & q_o"offset=${long(offset)}") =>
      val theLimit = limit.getOrElse(20L)
      val theOffset = offset.getOrElse(0L)

      articleController.all(PageRequest(theLimit, theOffset, List(Ordering(ArticleMetaModel.updatedAt, Descending))))
    case POST(p"/articles") => articleController.create
  }

  lazy val tagController: TagController = wire[TagController]
  protected lazy val tagService: TagService = wire[TagService]
  protected lazy val tagRepo: TagRepo = wire[TagRepo]

  val tagRoutes: Router.Routes = {
    case GET(p"/tags") => tagController.all
  }

}