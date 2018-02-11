package core.articles.repositories

import commons.models.{Email, Page}
import commons.utils.DbioUtils
import core.articles.exceptions.MissingArticleException
import core.articles.models._
import core.users.models.{Profile, User, UserId}
import core.users.repositories.{ProfileRepo, UserRepo}
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

    for {
      maybeArticleWithAuthor <- articleRepo.findBySlugWithAuthor(slug)
      (article, author) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      articleWithTags <- getArticleWithTags(article, author, maybeUserEmail)
    } yield articleWithTags
  }

  def getArticleWithTags(article: Article, author: User, maybeCurrentUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    for {
      tags <- articleTagRepo.findTagsByArticleId(article.id)
      profile <- profileRepo.findByUser(author, maybeCurrentUserEmail)
      favorited <- isFavorited(article.id, maybeCurrentUserEmail)
      favoritesCount <- getFavoritesCount(article.id)
    } yield createArticleWithTags(article, profile, tags, favorited, favoritesCount)
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

  def getArticleWithTags(article: Article, author: User, tags: Seq[Tag],
                         maybeCurrentUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    for {
      profile <- profileRepo.findByUser(author, maybeCurrentUserEmail)
      favorited <- isFavorited(article.id, maybeCurrentUserEmail)
      favoritesCount <- getFavoritesCount(article.id)
    } yield createArticleWithTags(article, profile, tags, favorited, favoritesCount)
  }

  def findFeed(pageRequest: UserFeedPageRequest, currentUserEmail: Email): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && currentUserEmail != null)

    val articlesPageAction = userRepo.findByEmail(currentUserEmail)
      .flatMap(currentUser => articleRepo.findByUserFeedPageRequest(pageRequest, currentUser.id))

    getArticlesWithTagsPage(articlesPageAction, Some(currentUserEmail))
  }

  private def getArticlesWithTagsPage(articlesPageAction: DBIO[Page[(Article, User)]],
                                      maybeCurrentUserEmail: Option[Email]) = {
    for {
      Page(articlesWithUsers, count) <- articlesPageAction
      (articles, authors) = articlesWithUsers.unzip
      profileByUserId <- profileRepo.getProfileByUserId(authors, maybeCurrentUserEmail)
      tagsByArticleId <- getGroupedTagsByArticleId(articlesWithUsers)
      favoritedArticleIds <- getFavoritedArticleIds(articlesWithUsers, maybeCurrentUserEmail)
      favoritesCountByArticleId <- getFavoritesCount(articlesWithUsers)
    } yield {
      val articlesWithTags = createArticlesWithTags(articles, profileByUserId, tagsByArticleId, favoritedArticleIds,
        favoritesCountByArticleId)

      Page(articlesWithTags, count)
    }
  }

  private def getFavoritedArticleIds(articlesWithUsers: Seq[(Article, User)],
                                     maybeFollowerEmail: Option[Email]): DBIO[Set[ArticleId]] = {
    maybeFollowerEmail.map(email => getFavoritedArticleIds(articlesWithUsers, email))
      .getOrElse(DBIO.successful(Set.empty))
  }

  private def getFavoritedArticleIds(articlesWithUsers: Seq[(Article, User)],
                                     followerEmail: Email): DBIO[Set[ArticleId]] = {
    for {
      follower <- userRepo.findByEmail(followerEmail)
      articleIds <- getFavoritedArticleIds(articlesWithUsers, follower)
    } yield articleIds
  }

  private def getFavoritedArticleIds(articlesWithUsers: Seq[(Article, User)], follower: User) = {
    val articleIds = articlesWithUsers.map(_._1.id)

    favoriteAssociationRepo.findByUserAndArticles(follower.id, articleIds)
      .map(_.map(_.favoritedId))
      .map(_.toSet)
  }

  private def getFavoritesCount(articlesWithUsers: Seq[(Article, User)]) = {
    val articleIds = articlesWithUsers.map(_._1.id)

    favoriteAssociationRepo.groupByArticleAndCount(articleIds)
      .map(_.toMap)
  }

  private def getGroupedTagsByArticleId(articlesWithUsers: Seq[(Article, User)]) = {
    val articleIds = articlesWithUsers.map(_._1.id)
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

      createArticleWithTags(article, profile, tags, favorited, favoritesCount)
    }

    articles.map(createArticleWithTagsHelper)
  }

  private def createArticleWithTags(article: Article, profile: Profile, tags: Seq[Tag], favorited: Boolean,
                                    favoritesCount: Int): ArticleWithTags = {
    val tagValues = tags.map(_.name)
    ArticleWithTags.fromTagValues(article, tagValues, profile, favorited, favoritesCount)
  }

  def findAll(pageRequest: MainFeedPageRequest, maybeCurrentUserEmail: Option[Email]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    val articlesPageAction = articleRepo.findByMainFeedPageRequest(pageRequest)

    getArticlesWithTagsPage(articlesPageAction, maybeCurrentUserEmail)
  }

}