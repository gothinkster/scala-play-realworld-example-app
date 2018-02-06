package core.articles.services

import com.github.slugify.Slugify
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
                     articleTagRepo: ArticleTagRepo,
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

  def create(newArticle: NewArticle, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(newArticle != null && currentUserEmail != null)

    for {
      user <- userRepo.byEmail(currentUserEmail)
      articleId <- createArticle(newArticle, user)
      (article, author) <- articleRepo.byIdWithUser(articleId)
      tags <- createTagsIfNotExist(newArticle)
      _ <- associateTagsWithArticle(article, tags)
      articleWithTag <- articleWithTagsRepo.getArticleWithTags(article, author, tags, Some(currentUserEmail))
    } yield articleWithTag
  }

  private def createArticle(newArticle: NewArticle, user: User) = {
    val slug = slugifier.slugify(newArticle.title)
    val article = newArticle.toArticle(slug, user.id, dateTimeProvider)
    articleRepo.insert(article)
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

  def update(slug: String, articleUpdate: ArticleUpdate, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(slug != null && articleUpdate != null && currentUserEmail != null)

    for {
      maybeArticleWithAuthor <- articleRepo.bySlugWithAuthor(slug)
      (article, author) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      updatedArticle <- doUpdate(article, articleUpdate)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(updatedArticle, author, Some(currentUserEmail))
    } yield articleWithTags
  }

  private def doUpdate(article: Article, articleUpdate: ArticleUpdate) = {
    val title = articleUpdate.maybeTitle.getOrElse(article.title)
    val slug = slugifier.slugify(title)
    val description = articleUpdate.maybeDescription.getOrElse(article.description)
    val body = articleUpdate.maybeBody.getOrElse(article.body)
    val updatedArticle = article.copy(title = title, slug = slug, description = description, body = body,
      updatedAt = dateTimeProvider.now)

    articleRepo.update(updatedArticle)
      .map(_ => updatedArticle)
  }

  def all(pageRequest: MainFeedPageRequest, maybeCurrentUserEmail: Option[Email]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && maybeCurrentUserEmail != null)

    articleWithTagsRepo.all(pageRequest, maybeCurrentUserEmail)
  }

  def feed(pageRequest: UserFeedPageRequest, currentUserEmail: Email): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && currentUserEmail != null)

    articleWithTagsRepo.feed(pageRequest, currentUserEmail)
  }

  def delete(slug: String, currentUserEmail: Email): DBIO[Unit] = {
    require(slug != null && currentUserEmail != null)

    for {
      maybeArticle <- articleRepo.bySlug(slug)
      article <- DbioUtils.optionToDbio(maybeArticle, new MissingArticleException(slug))
      _ <- validate(currentUserEmail, article)
      _ <- deleteComments(article)
      _ <- deleteFavoriteAssociations(article)
      _ <- deleteArticle(article)
    } yield ()
  }

  private def validate(currentUserEmail: Email, article: Article) = {
    userRepo.byEmail(currentUserEmail).map(currentUser => {
      if (article.authorId == currentUser.id) DBIO.successful(())
      else DBIO.failed(new AuthorMismatchException(currentUser.id, article.authorId))
    })
  }

  private def deleteComments(article: Article) = {
    for {
      comments <- commentRepo.byArticleId(article.id)
      commentIds = comments.map(_.id)
      _ <- commentRepo.delete(commentIds)
    } yield ()
  }

  private def deleteFavoriteAssociations(article: Article) = {
    for {
      favoriteAssociations <- favoriteAssociationRepo.byArticle(article.id)
      favoriteAssociationIds = favoriteAssociations.map(_.id)
      _ <- favoriteAssociationRepo.delete(favoriteAssociationIds)
    } yield ()
  }

  private def deleteArticle(article: Article) = {
    articleRepo.delete(article.id)
  }

}
