package articles.services

import articles.controllers.{Page, PageRequest}
import articles.models.Article
import articles.repositories.ArticleRepo
import slick.dbio.DBIO

class ArticleService(articleRepo: ArticleRepo) {

  def all(pageRequest: PageRequest): DBIO[Page[Article]] = {
    articleRepo.byPageRequest(pageRequest)
  }

}
