package core.articles.repositories

import commons.models.{Email, Page}
import core.articles.models._
import core.users.models.User
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleWithTagsRepo(articleRepo: ArticleRepo,
                          articleTagRepo: ArticleTagRepo,
                          tagRepo: TagRepo,
                          userRepo: UserRepo,
                          implicit private val ex: ExecutionContext) {

  def feed(pageRequest: UserFeedPageRequest, followerEmail: Email): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && followerEmail != null)

    for {
      follower <- userRepo.byEmail(followerEmail)
      Page(articlesWithUsers, count) <- articleRepo.byUserFeedPageRequest(pageRequest, follower.id)
      groupedTags <- selectAndGroupTagsByArticleId(articlesWithUsers)
    } yield {
      val articlesWithTags = createArticlesWithTags(articlesWithUsers, groupedTags)

      Page(articlesWithTags, count)
    }
  }

  def all(pageRequest: MainFeedPageRequest): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    for {
      Page(articlesWithUsers, count) <- articleRepo.byMainFeedPageRequest(pageRequest)
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
