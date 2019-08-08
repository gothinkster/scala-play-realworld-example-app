package articles.services

import articles.models.{Article, ArticleWithTags, FavoriteAssociation, FavoriteAssociationId}
import articles.repositories.{ArticleRepo, ArticleWithTagsRepo, FavoriteAssociationRepo}
import slick.dbio.DBIO
import users.models.{User, UserId}
import users.repositories.UserRepo

import scala.concurrent.ExecutionContext

trait ArticleFavoriteService {
  protected val articleRepo: ArticleRepo
  protected val articleWithTagsRepo: ArticleWithTagsRepo
  protected val userRepo: UserRepo
  protected val favoriteAssociationRepo: FavoriteAssociationRepo

  implicit protected val ex: ExecutionContext

  def favorite(slug: String, userId: UserId): DBIO[ArticleWithTags] = {
    require(slug != null && userId != null)

    for {
      article <- articleRepo.findBySlug(slug)
      _ <- createFavoriteAssociation(userId, article)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(article, userId)
    } yield articleWithTags
  }

  private def createFavoriteAssociation(userId: UserId, article: Article) = {
    val favoriteAssociation = FavoriteAssociation(FavoriteAssociationId(-1), userId, article.id)
    favoriteAssociationRepo.insert(favoriteAssociation)
  }

  def unfavorite(slug: String, userId: UserId): DBIO[ArticleWithTags] = {
    require(slug != null && userId != null)

    for {
      article <- articleRepo.findBySlug(slug)
      _ <- deleteFavoriteAssociation(userId, article)
      articleWithTags <- articleWithTagsRepo.getArticleWithTags(article, userId)
    } yield articleWithTags
  }

  private def deleteFavoriteAssociation(userId: UserId, article: Article) = {
    favoriteAssociationRepo.findByUserAndArticle(userId, article.id)
      .flatMap(_.map(favoriteAssociation => favoriteAssociationRepo.delete(favoriteAssociation.id))
        .getOrElse(DBIO.successful(())))
  }

}
