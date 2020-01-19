package articles.controllers

import articles.controllers.ArticleController.{defaultLimit, defaultOffset}
import articles.exceptions.AuthorMismatchException
import articles.models._
import articles.services.{ArticleReadService, ArticleWriteService}
import commons.controllers.RealWorldAbstractController
import commons.exceptions.MissingModelException
import commons.models.{Descending, Ordering, Username}
import commons.services.ActionRunner
import org.apache.commons.lang3.StringUtils
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import users.controllers.{AuthenticatedActionBuilder, OptionallyAuthenticatedActionBuilder}

class ArticleController(authenticatedAction: AuthenticatedActionBuilder,
                        optionallyAuthenticatedActionBuilder: OptionallyAuthenticatedActionBuilder,
                        actionRunner: ActionRunner,
                        articleWriteService: ArticleWriteService,
                        articleReadService: ArticleReadService,
                        components: ControllerComponents)
  extends RealWorldAbstractController(components) {

  def unfavorite(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val userId = request.user.userId
    actionRunner.runTransactionally(articleWriteService.unfavorite(slug, userId))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def favorite(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val userId = request.user.userId
    actionRunner.runTransactionally(articleWriteService.favorite(slug, userId))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def findBySlug(slug: String): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    require(StringUtils.isNotBlank(slug))

    val maybeUserId = request.authenticatedUserOption.map(_.userId)
    actionRunner.runTransactionally(articleReadService.findBySlug(slug, maybeUserId))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover({
        case _: MissingModelException => NotFound
      })
  }

  def findAll(maybeTag: Option[String],
              maybeAuthor: Option[String],
              maybeFavorited: Option[String],
              maybeLimit: Option[Long],
              maybeOffset: Option[Long]): Action[AnyContent] = optionallyAuthenticatedActionBuilder.async { request =>
    validateNoMoreThanOneFilterIsGiven(maybeTag, maybeAuthor, maybeFavorited)

    val pageRequest = buildPageRequest(maybeTag, maybeAuthor, maybeFavorited, maybeLimit, maybeOffset)
    val maybeUserId = request.authenticatedUserOption.map(_.userId)
    actionRunner.runTransactionally(articleReadService.findAll(pageRequest, maybeUserId))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  private def buildPageRequest(maybeTag: Option[String], maybeAuthor: Option[String],
                               maybeFavorited: Option[String], maybeLimit: Option[Long],
                               maybeOffset: Option[Long]) = {
    val limit = maybeLimit.getOrElse(defaultLimit)
    val offset = maybeOffset.getOrElse(defaultOffset)
    if (maybeTag.isDefined) {
      ArticlesByTag(maybeTag.get, limit, offset)
    } else if (maybeAuthor.isDefined) {
      ArticlesByAuthor(maybeAuthor.map(Username(_)).get, limit, offset)
    } else if (maybeFavorited.isDefined) {
      ArticlesByFavorited(maybeFavorited.map(Username(_)).get, limit, offset)
    } else {
      ArticlesAll(limit, offset)
    }
  }

  private def validateNoMoreThanOneFilterIsGiven(maybeTag: Option[String], maybeAuthor: Option[String], maybeFavorited: Option[String]) = {
    val possibleFilters = List(maybeTag, maybeAuthor, maybeFavorited)
    val filtersCount = possibleFilters.count(_.isDefined)
    if (filtersCount >= 2) {
      BadRequest("Can not use more than one filter at the time")
    }
  }

  def findFeed(maybeLimit: Option[Long], maybeOffset: Option[Long]): Action[AnyContent] = authenticatedAction.async { request =>
    val pageRequest: UserFeedPageRequest = buildPageRequest(maybeLimit, maybeOffset)
    val userId = request.user.userId
    actionRunner.runTransactionally(articleReadService.findFeed(pageRequest, userId))
      .map(page => ArticlePage(page.models, page.count))
      .map(Json.toJson(_))
      .map(Ok(_))
  }

  private def buildPageRequest(maybeLimit: Option[Long], maybeOffset: Option[Long]) = {
    val limit = maybeLimit.getOrElse(20L)
    val offset = maybeOffset.getOrElse(0L)
    val pageRequest = UserFeedPageRequest(limit, offset,
      List(Ordering(ArticleMetaModel.createdAt, Descending)))
    pageRequest
  }

  def create: Action[NewArticleWrapper] = authenticatedAction.async(validateJson[NewArticleWrapper]) { request =>
    val article = request.body.article
    val userId = request.user.userId
    actionRunner.runTransactionally(articleWriteService.create(article, userId))
      .map(ArticleWrapper(_))
      .map(Json.toJson(_))
      .map(Ok(_))
      .recover(handleFailedValidation)
  }

  def update(slug: String): Action[ArticleUpdateWrapper] = {
    authenticatedAction.async(validateJson[ArticleUpdateWrapper]) { request =>
      val articleUpdate = request.body.article
      val userId = request.user.userId
      actionRunner.runTransactionally(articleWriteService.update(slug, articleUpdate, userId))
        .map(ArticleWrapper(_))
        .map(Json.toJson(_))
        .map(Ok(_))
        .recover(handleFailedValidation.orElse({
          case _: MissingModelException => NotFound
        }))
    }
  }

  def delete(slug: String): Action[AnyContent] = authenticatedAction.async { request =>
    require(slug != null)

    val userId = request.user.userId
    actionRunner.runTransactionally(articleWriteService.delete(slug, userId))
      .map(_ => Ok)
      .recover({
        case _: AuthorMismatchException => Forbidden
        case _: MissingModelException => NotFound
      })
  }

}

object ArticleController {
  private lazy val defaultOffset = 0L
  private lazy val defaultLimit = 20L
}
