package core.articles.services

import core.articles.models._
import core.articles.repositories.{ArticleRepo, ArticleTagRepo, ArticleWithTagsRepo, TagRepo}
import commons.models.{Page, PageRequest}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleService(articleRepo: ArticleRepo,
                     articleWithTagsRepo: ArticleWithTagsRepo,
                     articleTagRepo: ArticleTagRepo,
                     tagRepo: TagRepo,
                     implicit private val ex: ExecutionContext) {

  def create(newArticle: NewArticle): DBIO[ArticleWithTags] = {
    require(newArticle != null)

    articleRepo.create(newArticle.toArticle)
      .zip(createTags(newArticle))
      .flatMap(associateTagsWithArticle)
  }

  private def associateTagsWithArticle(articleAndTags: (Article, Seq[Tag])) = {
    val (article, tags) = articleAndTags

    val articleTags = tags.map(tag => ArticleTag.from(article, tag))

    articleTagRepo.create(articleTags)
      .map(_ => ArticleWithTags(article, tags))
  }

  private def createTags(newArticle: NewArticle) = {
    val tags =  newArticle.tags
      .map(Tag.from)

    tagRepo.create(tags)
  }

  def all(pageRequest: PageRequest): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    articleWithTagsRepo.all(pageRequest)
  }

}
