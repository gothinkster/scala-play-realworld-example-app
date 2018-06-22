package core.articles.services

import commons.models.Email
import core.articles.exceptions.AuthorMismatchException
import core.articles.models.Article
import core.articles.repositories._
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

protected trait ArticleDeleteService {
  protected val articleRepo: ArticleRepo
  protected val articleTagAssociationRepo: ArticleTagAssociationRepo
  protected val articleWithTagsRepo: ArticleWithTagsRepo
  protected val userRepo: UserRepo
  protected val commentRepo: CommentRepo
  protected val favoriteAssociationRepo: FavoriteAssociationRepo

  implicit protected val ex: ExecutionContext

  def delete(slug: String, currentUserEmail: Email): DBIO[Unit] = {
    require(slug != null && currentUserEmail != null)

    for {
      article <- articleRepo.findBySlug(slug)
      _ <- validate(currentUserEmail, article)
      _ <- deleteComments(article)
      _ <- deleteArticleTags(article)
      _ <- deleteFavoriteAssociations(article)
      _ <- deleteArticle(article)
    } yield ()
  }

  private def validate(currentUserEmail: Email, article: Article) = {
    userRepo.findByEmail(currentUserEmail).map(currentUser => {
      if (article.authorId == currentUser.id) DBIO.successful(())
      else DBIO.failed(new AuthorMismatchException(currentUser.id, article.authorId))
    })
  }

  private def deleteComments(article: Article) = {
    for {
      comments <- commentRepo.findByArticleId(article.id)
      commentIds = comments.map(_.id)
      _ <- commentRepo.delete(commentIds)
    } yield ()
  }

  private def deleteArticleTags(article: Article) = {
    for {
      articleTags <- articleTagAssociationRepo.findByArticleId(article.id)
      articleTagIds = articleTags.map(_.id)
      _ <- articleTagAssociationRepo.delete(articleTagIds)
    } yield ()
  }

  private def deleteFavoriteAssociations(article: Article) = {
    for {
      favoriteAssociations <- favoriteAssociationRepo.findByArticle(article.id)
      favoriteAssociationIds = favoriteAssociations.map(_.id)
      _ <- favoriteAssociationRepo.delete(favoriteAssociationIds)
    } yield ()
  }

  private def deleteArticle(article: Article) = {
    articleRepo.delete(article.id)
  }

}
