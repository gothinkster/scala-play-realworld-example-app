package articles.repositories

import commons.models.{Email, Page}
import articles.models._
import users.models.{Profile, User, UserId}
import users.repositories.{ProfileRepo, UserRepo}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleWithTagsRepo(articleRepo: ArticleRepo,
                          articleTagRepo: ArticleTagAssociationRepo,
                          tagRepo: TagRepo,
                          userRepo: UserRepo,
                          favoriteAssociationRepo: FavoriteAssociationRepo,
                          profileRepo: ProfileRepo,
                          implicit private val ex: ExecutionContext) {

  def findBySlug(slug: String, maybeUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    require(slug != null && maybeUserEmail != null)

    articleRepo.findBySlug(slug)
      .flatMap(article => getArticleWithTags(article, maybeUserEmail))
  }

  private def getArticleWithTags(article: Article, maybeCurrentUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    for {
      tags <- articleTagRepo.findTagsByArticleId(article.id)
      profile <- profileRepo.findByUserId(article.authorId, maybeCurrentUserEmail)
      favorited <- isFavorited(article.id, maybeCurrentUserEmail)
      favoritesCount <- getFavoritesCount(article.id)
    } yield ArticleWithTags(article, tags, profile, favorited, favoritesCount)
  }

  private def isFavorited(articleId: ArticleId, maybeEmail: Option[Email]): DBIO[Boolean] = {
    maybeEmail.map(email => isFavorited(articleId, email))
      .getOrElse(DBIO.successful(false))
  }

  private def isFavorited(articleId: ArticleId, email: Email) = {
    for {
      user <- userRepo.findByEmail(email)
      maybeFavoriteAssociation <- favoriteAssociationRepo.findByUserAndArticle(user.id, articleId)
    } yield maybeFavoriteAssociation.isDefined
  }

  private def getFavoritesCount(articleId: ArticleId) = {
    favoriteAssociationRepo.groupByArticleAndCount(Seq(articleId))
      .map(_.find(_._1 == articleId))
      .map(_.map(_._2).getOrElse(0))
  }

  def getArticleWithTags(article: Article, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    getArticleWithTags(article, Some(currentUserEmail))
  }

  def getArticleWithTags(article: Article, tags: Seq[Tag], currentUserEmail: Email): DBIO[ArticleWithTags] = {
    userRepo.findById(article.authorId)
      .flatMap(author => getArticleWithTags(article, author, tags, Some(currentUserEmail)))
  }

  private def getArticleWithTags(article: Article, author: User, tags: Seq[Tag],
                                 maybeCurrentUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    for {
      profile <- profileRepo.findByUserId(author.id, maybeCurrentUserEmail)
      favorited <- isFavorited(article.id, maybeCurrentUserEmail)
      favoritesCount <- getFavoritesCount(article.id)
    } yield ArticleWithTags(article, tags, profile, favorited, favoritesCount)
  }

  def findFeed(pageRequest: UserFeedPageRequest, currentUserEmail: Email): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && currentUserEmail != null)

    getArticlesPage(pageRequest, currentUserEmail)
      .flatMap(articlesPage => getArticlesWithTagsPage(articlesPage, Some(currentUserEmail)))
  }

  private def getArticlesPage(pageRequest: UserFeedPageRequest, currentUserEmail: Email) = {
    userRepo.findByEmail(currentUserEmail)
      .flatMap(currentUser => articleRepo.findByUserFeedPageRequest(pageRequest, currentUser.id))
  }

  def findAll(pageRequest: MainFeedPageRequest, maybeCurrentUserEmail: Option[Email]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    articleRepo.findByMainFeedPageRequest(pageRequest)
      .flatMap(articlesPage => getArticlesWithTagsPage(articlesPage, maybeCurrentUserEmail))
  }

  private def getArticlesWithTagsPage(articlesPage: Page[Article],
                                      maybeCurrentUserEmail: Option[Email]) = {
    val Page(articles, count) = articlesPage
    for {
      profileByUserId <- getProfileByUserId(articles, maybeCurrentUserEmail)
      tagsByArticleId <- getGroupedTagsByArticleId(articles)
      favoritedArticleIds <- getFavoritedArticleIds(articles, maybeCurrentUserEmail)
      favoritesCountByArticleId <- getFavoritesCount(articles)
    } yield {
      val articlesWithTags = createArticlesWithTags(articles, profileByUserId, tagsByArticleId, favoritedArticleIds,
        favoritesCountByArticleId)

      Page(articlesWithTags, count)
    }
  }

  private def getProfileByUserId(articles: Seq[Article], maybeCurrentUserEmail: Option[Email]) = {
    val authorIds = articles.map(_.authorId)
    profileRepo.getProfileByUserId(authorIds, maybeCurrentUserEmail)
  }

  private def getFavoritedArticleIds(articles: Seq[Article], maybeFollowerEmail: Option[Email]): DBIO[Set[ArticleId]] = {
    maybeFollowerEmail.map(email => getFavoritedArticleIds(articles, email))
      .getOrElse(DBIO.successful(Set.empty))
  }

  private def getFavoritedArticleIds(articles: Seq[Article], followerEmail: Email): DBIO[Set[ArticleId]] = {
    userRepo.findByEmail(followerEmail)
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