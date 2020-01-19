package articles.services

import articles.models.{ArticleWithTags, ArticlesPageRequest, UserFeedPageRequest}
import articles.repositories.ArticleWithTagsRepo
import commons.models.Page
import slick.dbio.DBIO
import users.models.UserId

class ArticleReadService(articleWithTagsRepo: ArticleWithTagsRepo) {

  def findBySlug(slug: String, maybeUserId: Option[UserId]): DBIO[ArticleWithTags] = {
    require(slug != null && maybeUserId != null)

    articleWithTagsRepo.findBySlug(slug, maybeUserId)
  }

  def findAll(pageRequest: ArticlesPageRequest, maybeUserId: Option[UserId]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && maybeUserId != null)

    articleWithTagsRepo.findAll(pageRequest, maybeUserId)
  }

  def findFeed(pageRequest: UserFeedPageRequest, userId: UserId): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && userId != null)

    articleWithTagsRepo.findFeed(pageRequest, userId)
  }

}