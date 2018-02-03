package core.articles

import com.softwaremill.macwire.wire
import commons.CommonsComponents
import commons.config.{WithControllerComponents, WithExecutionContext}
import commons.models._
import core.articles.controllers.{ArticleController, CommentController, TagController}
import core.articles.models.{ArticleMetaModel, CommentId, MainFeedPageRequest, UserFeedPageRequest}
import core.articles.repositories._
import core.articles.services.{ArticleService, CommentService, TagService}
import core.authentication.api.AuthenticatedActionBuilder
import core.users.UserComponents
import play.api.routing.Router
import play.api.routing.sird._

trait ArticleComponents
  extends WithControllerComponents
    with UserComponents
    with CommonsComponents
    with WithExecutionContext {

  private lazy val defaultOffset = 0L
  private lazy val defaultLimit = 20L

  def authenticatedAction: AuthenticatedActionBuilder

  lazy val articleController: ArticleController = wire[ArticleController]
  protected lazy val articleService: ArticleService = wire[ArticleService]
  protected lazy val articleRepo: ArticleRepo = wire[ArticleRepo]

  lazy val commentController: CommentController = wire[CommentController]
  protected lazy val commentService: CommentService = wire[CommentService]
  protected lazy val commentRepo: CommentRepo = wire[CommentRepo]

  val articleRoutes: Router.Routes = {
    case GET(p"/articles" ? q_o"limit=${long(limit)}" &
      q_o"offset=${long(offset)}" &
      q_o"tag=$tag" &
      q_o"author=$author" &
      q_o"favorited=$favorited") =>

      val theLimit = limit.getOrElse(defaultLimit)
      val theOffset = offset.getOrElse(defaultOffset)
      val authorUsername = author.map(Username(_))

      articleController.all(MainFeedPageRequest(tag, authorUsername, favorited, theLimit, theOffset,
        List(Ordering(ArticleMetaModel.createdAt, Descending))))
    case GET(p"/articles/feed" ? q_o"limit=${long(limit)}" & q_o"offset=${long(offset)}") =>
      val theLimit = limit.getOrElse(defaultLimit)
      val theOffset = offset.getOrElse(defaultOffset)

      articleController.feed(UserFeedPageRequest(theLimit, theOffset,
        List(Ordering(ArticleMetaModel.createdAt, Descending))))
    case GET(p"/articles/$slug") =>
      articleController.bySlug(slug)
    case POST(p"/articles") =>
      articleController.create
    case POST(p"/articles/$slug/comments") =>
      commentController.create(slug)
    case GET(p"/articles/$slug/comments") =>
      commentController.byArticleSlug(slug)
    case POST(p"/articles/$slug/favorite") =>
      articleController.favorite(slug)
    case DELETE(p"/articles/$_/comments/${long(id)}") =>
      commentController.delete(CommentId(id))
  }

  lazy val tagController: TagController = wire[TagController]
  protected lazy val tagService: TagService = wire[TagService]
  protected lazy val tagRepo: TagRepo = wire[TagRepo]

  protected lazy val articleTagRepo: ArticleTagRepo = wire[ArticleTagRepo]
  protected lazy val articleWithTagsRepo: ArticleWithTagsRepo = wire[ArticleWithTagsRepo]

  lazy val favoriteAssociationRepo: FavoriteAssociationRepo = wire[FavoriteAssociationRepo]

  val tagRoutes: Router.Routes = {
    case GET(p"/tags") => tagController.all
  }

}