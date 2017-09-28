package core.articles.services

import core.articles.models.{Article, NewArticle}
import core.articles.repositories.ArticleRepo
import commons.models.{Page, PageRequest}
import slick.dbio.DBIO

class ArticleService(articleRepo: ArticleRepo) {

  def create(newArticle: NewArticle): DBIO[Article] = {
    articleRepo.create(newArticle.toArticle)
  }

  def all(pageRequest: PageRequest): DBIO[Page[Article]] = {
    articleRepo.byPageRequest(pageRequest)
  }

}
