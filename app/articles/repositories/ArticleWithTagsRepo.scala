package articles.repositories

import articles.models._
import commons.models.Page
import slick.dbio.DBIO
import users.models.{Profile, User, UserId}
import users.repositories.{ProfileRepo, UserRepo}

import scala.concurrent.ExecutionContext

class ArticleWithTagsRepo(articleRepo: ArticleRepo,
                          articleTagRepo: ArticleTagAssociationRepo,
                          tagRepo: TagRepo,
                          userRepo: UserRepo,
                          favoriteAssociationRepo: FavoriteAssociationRepo,
                          profileRepo: ProfileRepo,
                          implicit private val ex: ExecutionContext) {

  def findBySlug(slug: String, maybeUserId: Option[UserId]): DBIO[ArticleWithTags] = {
    require(slug != null && maybeUserId != null)

    articleRepo.findBySlug(slug)
      .flatMap(article => getArticleWithTags(article, maybeUserId))
  }

  private def getArticleWithTags(article: Article, maybeUserId: Option[UserId]): DBIO[ArticleWithTags] = {
    for {
      tags <- articleTagRepo.findTagsByArticleId(article.id)
      profile <- profileRepo.findByUserId(article.authorId, maybeUserId)
      favorited <- isFavorited(article.id, maybeUserId)
      favoritesCount <- getFavoritesCount(article.id)
    } yield ArticleWithTags(article, tags, profile, favorited, favoritesCount)
  }

  private def isFavorited(articleId: ArticleId, maybeUserId: Option[UserId]): DBIO[Boolean] = {
    maybeUserId.map(userId => isFavorited(articleId, userId))
      .getOrElse(DBIO.successful(false))
  }

  private def isFavorited(articleId: ArticleId, userId: UserId) = {
    favoriteAssociationRepo.findByUserAndArticle(userId, articleId)
      .map(maybeFavoriteAssociation => maybeFavoriteAssociation.isDefined)
  }

  private def getFavoritesCount(articleId: ArticleId) = {
    favoriteAssociationRepo.groupByArticleAndCount(Seq(articleId))
      .map(_.find(_._1 == articleId))
      .map(_.map(_._2).getOrElse(0))
  }

  def getArticleWithTags(article: Article, userId: UserId): DBIO[ArticleWithTags] = {
    getArticleWithTags(article, Some(userId))
  }

  def getArticleWithTags(article: Article, tags: Seq[Tag], userId: UserId): DBIO[ArticleWithTags] = {
    userRepo.findById(article.authorId)
      .flatMap(author => getArticleWithTags(article, author, tags, Some(userId)))
  }

  private def getArticleWithTags(article: Article, author: User, tags: Seq[Tag],
                                 maybeUserId: Option[UserId]): DBIO[ArticleWithTags] = {
    for {
      profile <- profileRepo.findByUserId(author.id, maybeUserId)
      favorited <- isFavorited(article.id, maybeUserId)
      favoritesCount <- getFavoritesCount(article.id)
    } yield ArticleWithTags(article, tags, profile, favorited, favoritesCount)
  }

  def findFeed(pageRequest: UserFeedPageRequest, userId: UserId): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && userId != null)

    getArticlesPage(pageRequest, userId)
      .flatMap(articlesPage => getArticlesWithTagsPage(articlesPage, Some(userId)))
  }

  private def getArticlesPage(pageRequest: UserFeedPageRequest, userId: UserId) = {
    articleRepo.findByUserFeedPageRequest(pageRequest, userId)
  }

  def findAll(pageRequest: MainFeedPageRequest, maybeUserId: Option[UserId]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    articleRepo.findByMainFeedPageRequest(pageRequest)
      .flatMap(articlesPage => getArticlesWithTagsPage(articlesPage, maybeUserId))
  }

  private def getArticlesWithTagsPage(articlesPage: Page[Article],
                                      maybeUserId: Option[UserId]) = {
    val Page(articles, count) = articlesPage
    for {
      profileByUserId <- getProfileByUserId(articles, maybeUserId)
      tagsByArticleId <- getGroupedTagsByArticleId(articles)
      favoritedArticleIds <- getFavoritedArticleIds(articles, maybeUserId)
      favoritesCountByArticleId <- getFavoritesCount(articles)
    } yield {
      val articlesWithTags = createArticlesWithTags(articles, profileByUserId, tagsByArticleId, favoritedArticleIds,
        favoritesCountByArticleId)

      Page(articlesWithTags, count)
    }
  }

  private def getProfileByUserId(articles: Seq[Article], maybeUserId: Option[UserId]) = {
    val authorIds = articles.map(_.authorId)
    profileRepo.getProfileByUserId(authorIds, maybeUserId)
  }

  private def getFavoritedArticleIds(articles: Seq[Article], maybeFollowerId: Option[UserId]): DBIO[Set[ArticleId]] = {
    maybeFollowerId.map(id => getFavoritedArticleIds(articles, id))
      .getOrElse(DBIO.successful(Set.empty))
  }

  private def getFavoritedArticleIds(articles: Seq[Article], followerId: UserId): DBIO[Set[ArticleId]] = {
    userRepo.findById(followerId)
      .flatMap(follower => getFavoritedArticleIds(articles, follower))
  }

  private def getFavoritedArticleIds(articles: Seq[Article], follower: User) = {
    val articleIds = articles.map(_.id)
    favoriteAssociationRepo.findByUserAndArticles(follower.id, articleIds)
      .map(_.map(_.favoritedId))
      .map(_.toSet)
  }

  private def getFavoritesCount(articles: Seq[Article]) = {
    val articleIds = articles.map(_.id)
    favoriteAssociationRepo.groupByArticleAndCount(articleIds)
      .map(_.toMap)
  }

  private def getGroupedTagsByArticleId(articles: Seq[Article]) = {
    val articleIds = articles.map(_.id)
    articleTagRepo.findByArticleIds(articleIds)
      .map(_.groupBy(_.articleId))
  }

  private def createArticlesWithTags(articles: Seq[Article],
                                     profileByUserId: Map[UserId, Profile],
                                     tagsByArticleId: Map[ArticleId, Seq[ArticleIdWithTag]],
                                     favoritedArticleIds: Set[ArticleId],
                                     favoritesCountByArticleId: Map[ArticleId, Int]) = {

    def createArticleWithTagsHelper(article: Article) = {
      val tags = tagsByArticleId.getOrElse(article.id, Seq.empty).map(_.tag)
      val favorited = favoritedArticleIds.contains(article.id)
      val favoritesCount = favoritesCountByArticleId.getOrElse(article.id, 0)
      val profile = profileByUserId(article.authorId)

      ArticleWithTags(article, tags, profile, favorited, favoritesCount)
    }

    articles.map(createArticleWithTagsHelper)
  }

}