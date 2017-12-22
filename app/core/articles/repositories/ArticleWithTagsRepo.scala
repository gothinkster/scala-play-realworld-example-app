package core.articles.repositories

import commons.models.{Page, PageRequest}
import core.articles.models._
import core.users.models.User
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleWithTagsRepo(articleRepo: ArticleRepo,
                          articleTagRepo: ArticleTagRepo,
                          tagRepo: TagRepo,
                          implicit private val ex: ExecutionContext) {

  def all(pageRequest: PageRequest): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    for {
      Page(articlesWithUsers, count) <- articleRepo.byPageRequest(pageRequest)
      groupedTags <- selectAndGroupTagsByArticleId(articlesWithUsers)
    } yield {
      val articlesWithTags = createArticlesWithTags(articlesWithUsers, groupedTags)

      Page(articlesWithTags, count)
    }
  }

  private def selectAndGroupTagsByArticleId(articlesWithUsers: Seq[(Article, User)]) = {
    val articleIds = articlesWithUsers.map(_._1.id)
    articleTagRepo.byArticleIds(articleIds)
      .map(_.groupBy(_.articleId))
  }

  private def createArticlesWithTags(articlesWithAuthors: Seq[(Article, User)],
                                     groupedTags: Map[ArticleId, Seq[ArticleIdWithTag]]) = {
    articlesWithAuthors.map(createArticleWithTags(_, groupedTags))
  }

  private def createArticleWithTags(articleWithAuthor: (Article, User),
                                    groupedTags: Map[ArticleId, Seq[ArticleIdWithTag]]) = {
    val (article, user) = articleWithAuthor
    val tagValues = groupedTags.getOrElse(article.id, Seq.empty).map(_.tag.name)

    ArticleWithTags.fromTagValues(article, tagValues, user)
  }

}
