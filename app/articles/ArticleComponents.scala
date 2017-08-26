package articles

import articles.controllers.{ArticleController, PageRequest}
import articles.models.ArticleMetaModel
import articles.repositories.ArticleRepo
import articles.services.ArticleService
import com.softwaremill.macwire.wire
import commons.CommonsComponents
import commons.config.WithControllerComponents
import play.api.routing.Router
import play.api.routing.sird._

import scala.concurrent.ExecutionContext
import commons.models._

trait ArticleComponents extends WithControllerComponents with CommonsComponents with WithExecutionContext {
  lazy val articleController: ArticleController = wire[ArticleController]
  protected lazy val articleService: ArticleService = wire[ArticleService]
  protected lazy val articleRepo: ArticleRepo = wire[ArticleRepo]

  val articleRoutes: Router.Routes = {
    case GET(p"/articles" ? q_o"limit=${long(limit)}" & q_o"offset=${long(offset)}") =>
      val theLimit = limit.getOrElse(20L)
      val theOffset = offset.getOrElse(0L)

      articleController.all(PageRequest(theLimit, theOffset, List(Ordering(ArticleMetaModel.modifiedAt, Descending))))
  }
}

trait WithExecutionContext {
  def executionContext: ExecutionContext
}
