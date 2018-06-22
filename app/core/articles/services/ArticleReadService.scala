package core.articles.services

import commons.models.{Email, Page}
import core.articles.models.{ArticleWithTags, MainFeedPageRequest, UserFeedPageRequest}
import core.articles.repositories.ArticleWithTagsRepo
import slick.dbio.DBIO

class ArticleReadService(articleWithTagsRepo: ArticleWithTagsRepo) {

  def findBySlug(slug: String, maybeCurrentUserEmail: Option[Email]): DBIO[ArticleWithTags] = {
    require(slug != null && maybeCurrentUserEmail != null)

    articleWithTagsRepo.findBySlug(slug, maybeCurrentUserEmail)
  }

  def findAll(pageRequest: MainFeedPageRequest, maybeCurrentUserEmail: Option[Email]): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && maybeCurrentUserEmail != null)

    articleWithTagsRepo.findAll(pageRequest, maybeCurrentUserEmail)
  }

  def findFeed(pageRequest: UserFeedPageRequest, currentUserEmail: Email): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && currentUserEmail != null)

    articleWithTagsRepo.findFeed(pageRequest, currentUserEmail)
  }

}