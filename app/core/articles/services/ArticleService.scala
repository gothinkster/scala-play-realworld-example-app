package core.articles.services

import com.github.slugify.Slugify
import commons.exceptions.ValidationException
import commons.models.{Email, Page}
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import core.articles.exceptions.{AuthorMismatchException, MissingArticleException}
import core.articles.models._
import core.articles.repositories._
import core.users.models.User
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleService(articleRepo: ArticleRepo,
                     articleValidator: ArticleValidator,
                     articleTagRepo: ArticleTagAssociationRepo,
                     tagRepo: TagRepo,
                     dateTimeProvider: DateTimeProvider,
                     articleWithTagsRepo: ArticleWithTagsRepo,
                     favoriteAssociationRepo: FavoriteAssociationRepo,
                     userRepo: UserRepo,
                     commentRepo: CommentRepo,
                     implicit private val ex: ExecutionContext) {

  private val slugifier = new Slugify()

  def unfavorite(slug: String, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(slug != null && currentUserEmail != null)

    for {
      user <- userRepo.findByEmail(currentUserEmail)
      maybeArticleWithAuthor <- articleRepo.findBySlugWithAuthor(slug)
      (article, author) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      _ <- deleteFavoriteAssociation(user, article)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(article, author, Some(currentUserEmail))
    } yield articleWithTags
  }

  private def deleteFavoriteAssociation(user: User, article: Article) = {
    favoriteAssociationRepo.findByUserAndArticle(user.id, article.id)
      .flatMap(_.map(favoriteAssociation => favoriteAssociationRepo.delete(favoriteAssociation.id))
        .getOrElse(DBIO.successful(())))
  }

  def favorite(slug: String, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(slug != null && currentUserEmail != null)

    for {
      user <- userRepo.findByEmail(currentUserEmail)
      maybeArticleWithAuthor <- articleRepo.findBySlugWithAuthor(slug)
      (article, author) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      _ <- createFavoriteAssociation(user, article)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(article, author, Some(currentUserEmail))
    } yield articleWithTags
  }

  private def createFavoriteAssociation(user: User, article: Article) = {
    val favoriteAssociation = FavoriteAssociation(FavoriteAssociationId(-1), user.id, article.id)
    favoriteAssociationRepo.insert(favoriteAssociation)
  }

  def findBySlug(slug: String, maybeCurrentUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    require(slug != null && maybeCurrentUserEmail != null)

    articleWithTagsRepo.findBySlug(slug, maybeCurrentUserEmail)
  }

  def create(newArticle: NewArticle, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(newArticle != null && currentUserEmail != null)

    for {
      violations <- validate(newArticle)
      _ <- DbioUtils.fail(violations.isEmpty, new ValidationException(violations))
      user <- userRepo.findByEmail(currentUserEmail)
      articleId <- createArticle(newArticle, user)
      (article, author) <- articleRepo.findByIdWithUser(articleId)
      tags <- createTagsIfNotExist(newArticle)
      _ <- associateTagsWithArticle(article, tags)
      articleWithTag <- articleWithTagsRepo.getArticleWithTags(article, author, tags, Some(currentUserEmail))
    } yield articleWithTag
  }

  private def validate(newArticle: NewArticle) = {
    DBIO.successful(articleValidator.validateNewArticle(newArticle))
  }

  private def createArticle(newArticle: NewArticle, user: User) = {
    val slug = slugifier.slugify(newArticle.title)
    val article = newArticle.toArticle(slug, user.id, dateTimeProvider)
    articleRepo.insert(article)
  }

  private def associateTagsWithArticle(article: Article, tags: Seq[Tag]) = {
    val articleTags = tags.map(tag => ArticleTagAssociation.from(article, tag))

    articleTagRepo.insertAndGet(articleTags)
  }

  private def createTagsIfNotExist(newArticle: NewArticle) = {
    val tagNames = newArticle.tagList
    val tags = tagNames.map(Tag.from)

    tagRepo.createIfNotExist(tags)
  }

  def update(slug: String, articleUpdate: ArticleUpdate, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(slug != null && articleUpdate != null && currentUserEmail != null)

    for {
      violations <- validate(articleUpdate)
      _ <- DbioUtils.fail(violations.isEmpty, new ValidationException(violations))
      maybeArticleWithAuthor <- articleRepo.findBySlugWithAuthor(slug)
      (article, author) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      updatedArticle <- doUpdate(article, articleUpdate)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(updatedArticle, author, Some(currentUserEmail))
    } yield articleWithTags
  }

  private def validate(articleUpdate: ArticleUpdate) = {
    DBIO.successful(articleValidator.validateArticleUpdate(articleUpdate))
  }

  private def doUpdate(article: Article, articleUpdate: ArticleUpdate) = {
    val title = articleUpdate.title.getOrElse(article.title)
    val slug = slugifier.slugify(title)
    val description = articleUpdate.description.getOrElse(article.description)
    val body = articleUpdate.body.getOrElse(article.body)
    val updatedArticle = article.copy(title = title, slug = slug, description = description, body = body,
      updatedAt = dateTimeProvider.now)

    articleRepo.updateAndGet(updatedArticle)
      .map(_ => updatedArticle)
  }

  def findAll(pageRequest: MainFeedPageRequest, maybeCurrentUserEmail: Option[Email]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && maybeCurrentUserEmail != null)

    articleWithTagsRepo.findAll(pageRequest, maybeCurrentUserEmail)
  }

  def findFeed(pageRequest: UserFeedPageRequest, currentUserEmail: Email): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && currentUserEmail != null)

    articleWithTagsRepo.findFeed(pageRequest, currentUserEmail)
  }

  def delete(slug: String, currentUserEmail: Email): DBIO[Unit] = {
    require(slug != null && currentUserEmail != null)

    for {
      maybeArticle <- articleRepo.findBySlug(slug)
      article <- DbioUtils.optionToDbio(maybeArticle, new MissingArticleException(slug))
      _ <- validate(currentUserEmail, article)
      _ <- deleteComments(article)
      _ <- deleteArticleTags(article)
      _ <- deleteFavoriteAssociations(article)
      _ <- deleteArticle(article)
    } yield ()
  }

  private def validate(currentUserEmail: Email, article: Article) = {
    userRepo.findByEmail(currentUserEmail).map(currentUser => {
      if (article.authorId == currentUser.id) DBIO.successful(())
      else DBIO.failed(new AuthorMismatchException(currentUser.id, article.authorId))
    })
  }

  private def deleteComments(article: Article) = {
    for {
      comments <- commentRepo.findByArticleId(article.id)
      commentIds = comments.map(_.id)
      _ <- commentRepo.delete(commentIds)
    } yield ()
  }

  private def deleteArticleTags(article: Article) = {
    for {
      articleTags <- articleTagRepo.findByArticleId(article.id)
      articleTagIds = articleTags.map(_.id)
      _ <- articleTagRepo.delete(articleTagIds)
    } yield ()
  }

  private def deleteFavoriteAssociations(article: Article) = {
    for {
      favoriteAssociations <- favoriteAssociationRepo.findByArticle(article.id)
      favoriteAssociationIds = favoriteAssociations.map(_.id)
      _ <- favoriteAssociationRepo.delete(favoriteAssociationIds)
    } yield ()
  }

  private def deleteArticle(article: Article) = {
    articleRepo.delete(article.id)
  }

}
