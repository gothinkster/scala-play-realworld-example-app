package core.articles.services

import commons.models.Email
import core.articles.models.{Article, ArticleWithTags, FavoriteAssociation, FavoriteAssociationId}
import core.articles.repositories.{ArticleRepo, ArticleWithTagsRepo, FavoriteAssociationRepo}
import core.users.models.User
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

trait ArticleFavoriteService {
  protected val articleRepo: ArticleRepo
  protected val articleWithTagsRepo: ArticleWithTagsRepo
  protected val userRepo: UserRepo
  protected val favoriteAssociationRepo: FavoriteAssociationRepo

  implicit protected val ex: ExecutionContext

  def favorite(slug: String, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(slug != null && currentUserEmail != null)

    for {
      user <- userRepo.findByEmail(currentUserEmail)
      article <- articleRepo.findBySlug(slug)
      _ <- createFavoriteAssociation(user, article)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(article, currentUserEmail)
    } yield articleWithTags
  }

  private def createFavoriteAssociation(user: User, article: Article) = {
    val favoriteAssociation = FavoriteAssociation(FavoriteAssociationId(-1), user.id, article.id)
    favoriteAssociationRepo.insert(favoriteAssociation)
  }

  def unfavorite(slug: String, currentUserEmail: Email): DBIO[ArticleWithTags] = {
    require(slug != null && currentUserEmail != null)

    for {
      user <- userRepo.findByEmail(currentUserEmail)
      article <- articleRepo.findBySlug(slug)
      _ <- deleteFavoriteAssociation(user, article)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(article, currentUserEmail)
    } yield articleWithTags
  }

  private def deleteFavoriteAssociation(user: User, article: Article) = {
    favoriteAssociationRepo.findByUserAndArticle(user.id, article.id)
      .flatMap(_.map(favoriteAssociation => favoriteAssociationRepo.delete(favoriteAssociation.id))
        .getOrElse(DBIO.successful(())))
  }

}
