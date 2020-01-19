package articles

import com.softwaremill.macwire.wire
import commons.config.{WithControllerComponents, WithExecutionContextComponents}
import commons.models._
import articles.controllers.{ArticleController, CommentController, TagController}
import articles.models._
import articles.repositories._
import articles.services._
import commons.CommonsComponents
import users.UserComponents
import play.api.routing.Router
import play.api.routing.sird._
import users.controllers.AuthenticatedActionBuilder

trait ArticleComponents
  extends WithControllerComponents
    with UserComponents
    with CommonsComponents
    with WithExecutionContextComponents {

  def authenticatedAction: AuthenticatedActionBuilder

  lazy val articleController: ArticleController = wire[ArticleController]
  lazy val articleWriteService: ArticleWriteService = wire[ArticleWriteService]
  lazy val articleReadService: ArticleReadService = wire[ArticleReadService]
  lazy val articleRepo: ArticleRepo = wire[ArticleRepo]

  lazy val commentController: CommentController = wire[CommentController]
  lazy val commentService: CommentService = wire[CommentService]
  lazy val commentWithAuthorRepo: CommentWithAuthorRepo = wire[CommentWithAuthorRepo]
  lazy val commentRepo: CommentRepo = wire[CommentRepo]

  lazy val tagController: TagController = wire[TagController]
  lazy val tagService: TagService = wire[TagService]
  lazy val tagRepo: TagRepo = wire[TagRepo]

  lazy val articleTagRepo: ArticleTagAssociationRepo = wire[ArticleTagAssociationRepo]
  lazy val articleWithTagsRepo: ArticleWithTagsRepo = wire[ArticleWithTagsRepo]

  lazy val favoriteAssociationRepo: FavoriteAssociationRepo = wire[FavoriteAssociationRepo]

  val articleRoutes: Router.Routes = {
    case GET(p"/articles" ? q_o"limit=${long(maybeLimit)}" &
      q_o"offset=${long(maybeOffset)}" &
      q_o"tag=$maybeTag" &
      q_o"author=$maybeAuthor" &
      q_o"favorited =$maybeFavorited") =>

      articleController.findAll(maybeTag, maybeAuthor, maybeFavorited, maybeLimit, maybeOffset)
    case GET(p"/articles/feed" ? q_o"limit=${long(limit)}" & q_o"offset=${long(offset)}") =>
      articleController.findFeed(limit, offset)
    case GET(p"/articles/$slug") =>
      articleController.findBySlug(slug)
    case POST(p"/articles") =>
      articleController.create
    case PUT(p"/articles/$slug") =>
      articleController.update(slug)
    case DELETE(p"/articles/$slug") =>
      articleController.delete(slug)
    case POST(p"/articles/$slug/comments") =>
      commentController.create(slug)
    case GET(p"/articles/$slug/comments") =>
      commentController.findByArticleSlug(slug)
    case POST(p"/articles/$slug/favorite") =>
      articleController.favorite(slug)
    case DELETE(p"/articles/$slug/favorite") =>
      articleController.unfavorite(slug)
    case DELETE(p"/articles/$_/comments/${long(id)}") =>
      commentController.delete(CommentId(id))
    case GET(p"/tags") =>
      tagController.findAll
  }

}