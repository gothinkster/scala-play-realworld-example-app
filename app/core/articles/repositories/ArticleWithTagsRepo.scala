package core.articles.repositories

import commons.models.{Email, Page}
import commons.utils.DbioUtils
import core.articles.exceptions.MissingArticleException
import core.articles.models._
import core.users.models.User
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleWithTagsRepo(articleRepo: ArticleRepo,
                          articleTagRepo: ArticleTagRepo,
                          tagRepo: TagRepo,
                          userRepo: UserRepo,
                          favoriteAssociationRepo: FavoriteAssociationRepo,
                          implicit private val ex: ExecutionContext) {

  def bySlug(slug: String, maybeUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    require(slug != null && maybeUserEmail != null)

    for {
      maybeArticleWithAuthor <- articleRepo.bySlugWithAuthor(slug)
      (article, author) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      articleWithTags <- getArticleWithTags(article, author, maybeUserEmail)
    } yield articleWithTags
  }

  def getArticleWithTags(article: Article, author: User, maybeUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    for {
      tags <- articleTagRepo.byArticleId(article.id)
      favorited <- isFavorited(article.id, maybeUserEmail)
      favoritesCount <- getFavoritesCount(article.id)
    } yield ArticleWithTags(article, tags, author, favorited, favoritesCount)
  }

  private def isFavorited(articleId: ArticleId, email: Email) = {
    for {
      user <- userRepo.byEmail(email)
      maybeFavoriteAssociation <- favoriteAssociationRepo.byUserAndArticle(user.id, articleId)
    } yield maybeFavoriteAssociation.isDefined
  }

  private def isFavorited(articleId: ArticleId, maybeEmail: Option[Email]): DBIO[Boolean] = {
    maybeEmail.map(email => isFavorited(articleId, email))
      .getOrElse(DBIO.successful(false))
  }

  private def getFavoritedArticleIds(articlesWithUsers: Seq[(Article, User)], follower: User) = {
    val articleIds = articlesWithUsers.map(_._1.id)

    favoriteAssociationRepo.byUserAndArticles(follower.id, articleIds)
      .map(_.map(_.favoritedId))
      .map(_.toSet)
  }

  private def getFavoritedArticleIds(articlesWithUsers: Seq[(Article, User)],
                                     maybeFollowerEmail: Option[Email]): DBIO[Set[ArticleId]] = {
    maybeFollowerEmail.map(email => getFavoritedArticleIds(articlesWithUsers, email))
      .getOrElse(DBIO.successful(Set.empty))
  }

  private def getFavoritedArticleIds(articlesWithUsers: Seq[(Article, User)], email: Email): DBIO[Set[ArticleId]] = {
    for {
      follower <- userRepo.byEmail(email)
      articleIds <- getFavoritedArticleIds(articlesWithUsers, follower)
    } yield articleIds
  }

  private def getFavoritesCount(articlesWithUsers: Seq[(Article, User)]) = {
    val articleIds = articlesWithUsers.map(_._1.id)

    favoriteAssociationRepo.groupByArticleAndCount(articleIds)
      .map(_.toMap)
  }

  private def getFavoritesCount(articleId: ArticleId) = {
    favoriteAssociationRepo.groupByArticleAndCount(Seq(articleId))
      .map(_.find(_._1 == articleId))
      .map(_.map(_._2).getOrElse(0))
  }

  def feed(pageRequest: UserFeedPageRequest, currentUserEmail: Email): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && currentUserEmail != null)

    for {
      currentUser <- userRepo.byEmail(currentUserEmail)
      Page(articlesWithUsers, count) <- articleRepo.byUserFeedPageRequest(pageRequest, currentUser.id)
      groupedTags <- getGroupedTagsByArticleId(articlesWithUsers)
      favoritedArticleIds <- getFavoritedArticleIds(articlesWithUsers, currentUser)
      groupedFavoritesCount <- getFavoritesCount(articlesWithUsers)
    } yield {
      val articlesWithTags = createArticlesWithTags(articlesWithUsers, groupedTags, favoritedArticleIds,
        groupedFavoritesCount)

      Page(articlesWithTags, count)
    }
  }

  def all(pageRequest: MainFeedPageRequest, maybeCurrentUserEmail: Option[Email]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    for {
      Page(articlesWithUsers, count) <- articleRepo.byMainFeedPageRequest(pageRequest)
      groupedTags <- getGroupedTagsByArticleId(articlesWithUsers)
      favoritedArticleIds <- getFavoritedArticleIds(articlesWithUsers, maybeCurrentUserEmail)
      groupedFavoritesCount <- getFavoritesCount(articlesWithUsers)
    } yield {
      val articlesWithTags = createArticlesWithTags(articlesWithUsers, groupedTags, favoritedArticleIds,
        groupedFavoritesCount)

      Page(articlesWithTags, count)
    }
  }

  private def getGroupedTagsByArticleId(articlesWithUsers: Seq[(Article, User)]) = {
    val articleIds = articlesWithUsers.map(_._1.id)
    articleTagRepo.byArticleIds(articleIds)
      .map(_.groupBy(_.articleId))
  }

  private def createArticlesWithTags(articlesWithAuthors: Seq[(Article, User)],
                                     groupedTags: Map[ArticleId, Seq[ArticleIdWithTag]],
                                     favoritedArticleIds: Set[ArticleId],
                                     groupedFavoritesCount: Map[ArticleId, Int]) = {
    articlesWithAuthors.map(createArticleWithTags(_, groupedTags, favoritedArticleIds, groupedFavoritesCount))
  }

  private def createArticleWithTags(articleWithAuthor: (Article, User),
                                    groupedTags: Map[ArticleId, Seq[ArticleIdWithTag]],
                                    favoritedArticleIds: Set[ArticleId],
                                    groupedFavoritesCount: Map[ArticleId, Int]) = {
    val (article, user) = articleWithAuthor
    val tagValues = groupedTags.getOrElse(article.id, Seq.empty).map(_.tag.name)
    val favorited = favoritedArticleIds.contains(article.id)
    val favoritesCount = groupedFavoritesCount.getOrElse(article.id, 0)

    ArticleWithTags.fromTagValues(article, tagValues, user, favorited, favoritesCount)
  }

}