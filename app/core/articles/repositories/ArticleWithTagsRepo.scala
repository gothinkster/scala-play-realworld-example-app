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
                          articleTagRepo: ArticleTagRepo,
                          tagRepo: TagRepo,
                          userRepo: UserRepo,
                          favoriteAssociationRepo: FavoriteAssociationRepo,
                          profileRepo: ProfileRepo,
                          implicit private val ex: ExecutionContext) {

  def bySlug(slug: String, maybeUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    require(slug != null && maybeUserEmail != null)

    for {
      maybeArticleWithAuthor <- articleRepo.bySlugWithAuthor(slug)
      (article, author) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      articleWithTags <- getArticleWithTags(article, author, maybeUserEmail)
    } yield articleWithTags
  }

  def getArticleWithTags(article: Article, author: User, tags: Seq[Tag],
                         maybeCurrentUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    for {
      profile <- profileRepo.byUser(author, maybeCurrentUserEmail)
      favorited <- isFavorited(article.id, maybeCurrentUserEmail)
      favoritesCount <- getFavoritesCount(article.id)
    } yield ArticleWithTags(article, tags, profile, favorited, favoritesCount)
  }

  def getArticleWithTags(article: Article, author: User, maybeCurrentUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    for {
      tags <- articleTagRepo.byArticleId(article.id)
      profile <- profileRepo.byUser(author, maybeCurrentUserEmail)
      favorited <- isFavorited(article.id, maybeCurrentUserEmail)
      favoritesCount <- getFavoritesCount(article.id)
    } yield ArticleWithTags(article, tags, profile, favorited, favoritesCount)
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

  private def getFavoritedArticleIds(articlesWithUsers: Seq[(Article, User)], followerEmail: Email): DBIO[Set[ArticleId]] = {
    for {
      follower <- userRepo.byEmail(followerEmail)
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

    val articlesPageAction = userRepo.byEmail(currentUserEmail)
      .flatMap(currentUser => articleRepo.byUserFeedPageRequest(pageRequest, currentUser.id))

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

  def all(pageRequest: MainFeedPageRequest, maybeCurrentUserEmail: Option[Email]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    val articlesPageAction = articleRepo.byMainFeedPageRequest(pageRequest)

    getArticlesWithTagsPage(articlesPageAction, maybeCurrentUserEmail)
  }

  private def getGroupedTagsByArticleId(articlesWithUsers: Seq[(Article, User)]) = {
    val articleIds = articlesWithUsers.map(_._1.id)
    articleTagRepo.byArticleIds(articleIds)
      .map(_.groupBy(_.articleId))
  }

  private def createArticlesWithTags(articles: Seq[Article],
                                     profileByUserId: Map[UserId, Profile],
                                     tagsByArticleId: Map[ArticleId, Seq[ArticleIdWithTag]],
                                     favoritedArticleIds: Set[ArticleId],
                                     favoritesCountByArticleId: Map[ArticleId, Int]) = {
    articles.map(createArticleWithTags(_, profileByUserId, tagsByArticleId, favoritedArticleIds,
      favoritesCountByArticleId))
  }

  private def createArticleWithTags(article: Article,
                                    profileByUserId: Map[UserId, Profile],
                                    tagsByArticleId: Map[ArticleId, Seq[ArticleIdWithTag]],
                                    favoritedArticleIds: Set[ArticleId],
                                    favoritesCountByArticleId: Map[ArticleId, Int]) = {
    val tagValues = tagsByArticleId.getOrElse(article.id, Seq.empty).map(_.tag.name)
    val favorited = favoritedArticleIds.contains(article.id)
    val favoritesCount = favoritesCountByArticleId.getOrElse(article.id, 0)
    val profile = profileByUserId(article.authorId)

    ArticleWithTags.fromTagValues(article, tagValues, profile, favorited, favoritesCount)
  }

}