package core.articles.services

import commons.models.Email
import commons.repositories.DateTimeProvider
import core.articles.exceptions.AuthorMismatchException
import core.articles.models._
import core.articles.repositories._
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class CommentService(articleRepo: ArticleRepo,
                     userRepo: UserRepo,
                     commentRepo: CommentRepo,
                     dateTimeProvider: DateTimeProvider,
                     commentWithAuthorRepo: CommentWithAuthorRepo,
                     implicit private val ex: ExecutionContext) {

  def delete(id: CommentId, email: Email): DBIO[Unit] = {
    require(email != null)

    for {
      _ <- validateAuthor(id, email)
      _ <- commentRepo.delete(id)
    } yield ()
  }

  private def validateAuthor(id: CommentId, email: Email) = {
    for {
      user <- userRepo.findByEmail(email)
      comment <- commentRepo.findById(id)
      _ <-
        if (user.id == comment.authorId) DBIO.successful(())
        else DBIO.failed(new AuthorMismatchException(user.id, comment.id))
    } yield ()
  }

  def findByArticleSlug(slug: String, maybeCurrentUserEmail: Option[Email]): DBIO[Seq[CommentWithAuthor]] = {
    require(slug != null && maybeCurrentUserEmail != null)

    commentWithAuthorRepo.findByArticleSlug(slug, maybeCurrentUserEmail)
  }

  def create(newComment: NewComment, slug: String, currentUserEmail: Email): DBIO[CommentWithAuthor] = {
    require(newComment != null && slug != null && currentUserEmail != null)

    for {
      comment <- doCreate(newComment, slug, currentUserEmail)
      commentWithAuthor <- commentWithAuthorRepo.getCommentWithAuthor(comment, currentUserEmail)
    } yield commentWithAuthor
  }

  private def doCreate(newComment: NewComment, slug: String, currentUserEmail: Email) = {
    for {
      article <- articleRepo.findBySlug(slug)
      currentUser <- userRepo.findByEmail(currentUserEmail)
      now = dateTimeProvider.now
      comment = Comment(CommentId(-1), article.id, currentUser.id, newComment.body, now, now)
      savedComment <- commentRepo.insertAndGet(comment)
    } yield savedComment
  }

}