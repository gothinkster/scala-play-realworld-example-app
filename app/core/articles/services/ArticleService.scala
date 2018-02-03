package core.articles.services

import com.github.slugify.Slugify
import commons.models.{Email, Page}
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import core.articles.exceptions.MissingArticleException
import core.articles.models._
import core.articles.repositories._
import core.users.models.User
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleService(articleRepo: ArticleRepo,
                     articleTagRepo: ArticleTagRepo,
                     tagRepo: TagRepo,
                     dateTimeProvider: DateTimeProvider,
                     articleWithTagsRepo: ArticleWithTagsRepo,
                     favoriteAssociationRepo: FavoriteAssociationRepo,
                     userRepo: UserRepo,
                     implicit private val ex: ExecutionContext) {

  def unfavorite(slug: String, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(slug != null && currentUserEmail != null)

    for {
      user <- userRepo.byEmail(currentUserEmail)
      maybeArticleWithAuthor <- articleRepo.bySlugWithAuthor(slug)
      (article, author) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      _ <- deleteFavoriteAssociation(user, article)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(article, author, Some(currentUserEmail))
    } yield articleWithTags
  }

  private def deleteFavoriteAssociation(user: User, article: Article) = {
    favoriteAssociationRepo.byUserAndArticle(user.id, article.id)
      .map(_.map(favoriteAssociation => favoriteAssociationRepo.delete(favoriteAssociation.id)))
  }

  def favorite(slug: String, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(slug != null && currentUserEmail != null)

    for {
      user <- userRepo.byEmail(currentUserEmail)
      maybeArticleWithAuthor <- articleRepo.bySlugWithAuthor(slug)
      (article, author) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      _ <- createFavoriteAssociation(user, article)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(article, author, Some(currentUserEmail))
    } yield articleWithTags
  }

  private def createFavoriteAssociation(user: User, article: Article) = {
    val favoriteAssociation = FavoriteAssociation(FavoriteAssociationId(-1), user.id, article.id)
    favoriteAssociationRepo.insert(favoriteAssociation)
  }

  def bySlug(slug: String, maybeCurrentUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    require(slug != null && maybeCurrentUserEmail != null)

    articleWithTagsRepo.bySlug(slug, maybeCurrentUserEmail)
  }

  def create(newArticle: NewArticle, user: User): DBIO[ArticleWithTags] = {
    require(newArticle != null && user != null)

    val article = createArticle(newArticle, user)

    for {
      articleId <- articleRepo.insert(article)
      (article, user) <- articleRepo.byIdWithUser(articleId)
      tags <- createTagsIfNotExist(newArticle)
      _ <- associateTagsWithArticle(article, tags)
    } yield ArticleWithTags(article, tags, user, favorited = false, 0)
  }

  private def createArticle(newArticle: NewArticle, user: User) = {
    val slugifier = new Slugify()
    val slug = slugifier.slugify(newArticle.title)
    newArticle.toArticle(slug, user.id, dateTimeProvider)
  }

  private def associateTagsWithArticle(article: Article, tags: Seq[Tag]) = {
    val articleTags = tags.map(tag => ArticleTag.from(article, tag))

    articleTagRepo.create(articleTags)
  }

  private def createTagsIfNotExist(newArticle: NewArticle) = {
    val tagNames = newArticle.tagList
    val tags = tagNames.map(Tag.from)

    tagRepo.createIfNotExist(tags)
  }

  def all(pageRequest: MainFeedPageRequest, maybeCurrentUserEmail: Option[Email]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && maybeCurrentUserEmail != null)

    articleWithTagsRepo.all(pageRequest, maybeCurrentUserEmail)
  }

  def feed(pageRequest: UserFeedPageRequest, currentUserEmail: Email): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && currentUserEmail != null)

    articleWithTagsRepo.feed(pageRequest, currentUserEmail)
  }

}
