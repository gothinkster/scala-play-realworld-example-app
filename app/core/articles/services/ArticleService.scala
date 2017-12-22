package core.articles.services

import commons.models.{Page, PageRequest}
import commons.repositories.DateTimeProvider
import core.articles.models._
import core.articles.repositories.{ArticleRepo, ArticleTagRepo, ArticleWithTagsRepo, TagRepo}
import core.users.models.User
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleService(articleRepo: ArticleRepo,
                     articleTagRepo: ArticleTagRepo,
                     tagRepo: TagRepo,
                     dateTimeProvider: DateTimeProvider,
                     articleWithTagsRepo: ArticleWithTagsRepo,
                     implicit private val ex: ExecutionContext) {

  def create(newArticle: NewArticle, user: User): DBIO[ArticleWithTags] = {
    require(newArticle != null && user != null)

    val article = newArticle.toArticle(user.id, dateTimeProvider)

    for {
      articleId <- articleRepo.insert(article)
      (article, user) <- articleRepo.byIdWithUser(articleId)
      tags <- createTagsIfNotExist(newArticle)
      _ <- associateTagsWithArticle(article, tags)
    } yield ArticleWithTags(article, tags, user)
  }

  private def associateTagsWithArticle(article: Article, tags: Seq[Tag]) = {
    val articleTags = tags.map(tag => ArticleTag.from(article, tag))

    articleTagRepo.create(articleTags)
  }

  private def createTagsIfNotExist(newArticle: NewArticle) = {
    val tagNames = newArticle.tags
    val tags = tagNames.map(Tag.from)

    tagRepo.createIfNotExist(tags)
  }

  def all(pageRequest: PageRequest): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    articleWithTagsRepo.all(pageRequest)
  }

}
