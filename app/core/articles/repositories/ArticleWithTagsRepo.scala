package core.articles.repositories

import commons.models.{Page, PageRequest}
import core.articles.models._
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleWithTagsRepo(articleRepo: ArticleRepo,
                          articleTagRepo: ArticleTagRepo,
                          tagRepo: TagRepo,
                          implicit private val ex: ExecutionContext) {

  def create(newArticle: NewArticle): DBIO[ArticleWithTags] = {
    require(newArticle != null)

    articleRepo.create(newArticle.toArticle)
      .zip(createTags(newArticle.tags))
      .map(createArticleWithTags)
  }

  private def createArticleWithTags(articleAndTags: (Article, Seq[Tag])) = {
    val (article, tags) = articleAndTags
    ArticleWithTags(article, tags)
  }

  def createTags(tagValues: Seq[String]): DBIO[Seq[Tag]] = {
    val tags = tagValues.map(Tag.from)
    tagRepo.create(tags)
  }

  def all(pageRequest: PageRequest): DBIO[Page[ArticleWithTags]] = {
    articleRepo.byPageRequest(pageRequest)
      .flatMap(createArticlesWithTags)
  }

  private def createArticlesWithTags(page: Page[Article]): DBIO[Page[ArticleWithTags]] = {
    findArticleTagsByArticleIds(page)
      .map(_.groupBy(_.articleId))
      .map(groupedTags => createArticlesWithTags(page, groupedTags))
      .map(articlesWithTags => Page(articlesWithTags, page.count))
  }

  private def findArticleTagsByArticleIds(page: Page[Article]) = {
    val ids = page.models.map(_.id)
    articleTagRepo.byArticleIds(ids)
  }

  private def createArticlesWithTags(page: Page[Article], groupedTags: Map[ArticleId, Seq[ArticleIdWithTag]]) = {
    val articles = page.models
    articles.map(createArticleWithTags(_, groupedTags))
  }

  private def createArticleWithTags(article: Article, groupedTags: Map[ArticleId, Seq[ArticleIdWithTag]]) = {
    val tagValues = groupedTags.getOrElse(article.id, Seq.empty).map(_.tag.name)
    ArticleWithTags.fromArticleAndRawTags(article, tagValues)
  }

}
