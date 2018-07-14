package articles.services

import commons.exceptions.ValidationException
import commons.models.Email
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import articles.models._
import articles.repositories.{ArticleRepo, ArticleTagAssociationRepo, ArticleWithTagsRepo, TagRepo}
import users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

trait ArticleCreateUpdateService {
  protected val articleRepo: ArticleRepo
  protected val articleTagAssociationRepo: ArticleTagAssociationRepo
  protected val tagRepo: TagRepo
  protected val dateTimeProvider: DateTimeProvider
  protected val articleWithTagsRepo: ArticleWithTagsRepo
  protected val userRepo: UserRepo

  private val articleValidator = new ArticleValidator
  private val slugifier = new Slugifier()

  def create(newArticle: NewArticle, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(newArticle != null && currentUserEmail != null)

    for {
      _ <- validate(newArticle)
      article <- createArticle(newArticle, currentUserEmail)
      tags <- handleTags(newArticle.tagList, article)
      articleWithTag <- articleWithTagsRepo.getArticleWithTags(article, tags, currentUserEmail)
    } yield articleWithTag
  }

  private def validate(newArticle: NewArticle) = {
    DBIO.successful(articleValidator.validateNewArticle(newArticle))
      .flatMap(violations => DbioUtils.fail(violations.isEmpty, new ValidationException(violations)))
  }

  private def createArticle(newArticle: NewArticle, currentUserEmail: Email) = {
    for {
      user <- userRepo.findByEmail(currentUserEmail)
      slug = slugifier.slugify(newArticle.title)
      article = newArticle.toArticle(slug, user.id, dateTimeProvider)
      persistedArticle <- articleRepo.insertAndGet(article)
    } yield persistedArticle
  }

  private def handleTags(tagNames: Seq[String], article: Article) = {
    for {
      existingTags <- tagRepo.findByNames(tagNames)
      newTags <- createTagsIfNotExist(tagNames, existingTags)
      tags = existingTags ++ newTags
      _ <- associateTagsWithArticle(tags, article)
    } yield tags
  }

  private def associateTagsWithArticle(tags: Seq[Tag], article: Article) = {
    val articleTags = tags.map(tag => ArticleTagAssociation.from(article, tag))

    articleTagAssociationRepo.insertAndGet(articleTags)
  }

  private def createTagsIfNotExist(tagNames: Seq[String], existingTags: Seq[Tag]) = {
    val existingTagNames = existingTags.map(_.name).toSet
    val newTagNames = tagNames.toSet -- existingTagNames
    val newTags = newTagNames.map(Tag.from)

    tagRepo.insertAndGet(newTags)
  }

  def update(slug: String, articleUpdate: ArticleUpdate, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(slug != null && articleUpdate != null && currentUserEmail != null)

    for {
      _ <- validate(articleUpdate)
      updatedArticle <- doUpdate(slug, articleUpdate)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(updatedArticle, currentUserEmail)
    } yield articleWithTags
  }

  private def validate(articleUpdate: ArticleUpdate) = {
    DBIO.successful(articleValidator.validateArticleUpdate(articleUpdate))
      .flatMap(violations => DbioUtils.fail(violations.isEmpty, new ValidationException(violations)))
  }

  private def doUpdate(slug: String, articleUpdate: ArticleUpdate) = {
    articleRepo.findBySlug(slug).flatMap(article => {
      val updatedArticle = createUpdatedArticle(articleUpdate, article)
      articleRepo.updateAndGet(updatedArticle)
    })
  }

  private def createUpdatedArticle(articleUpdate: ArticleUpdate, article: Article) = {
    val title = articleUpdate.title.getOrElse(article.title)
    val slug = slugifier.slugify(title)
    val description = articleUpdate.description.getOrElse(article.description)
    val body = articleUpdate.body.getOrElse(article.body)
    val updatedArticle = article.copy(title = title, slug = slug, description = description, body = body,
      updatedAt = dateTimeProvider.now)
    updatedArticle
  }

  implicit protected val ex: ExecutionContext

}
