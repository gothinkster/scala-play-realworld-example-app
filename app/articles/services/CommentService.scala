package articles.services

import articles.exceptions.AuthorMismatchException
import articles.models._
import articles.repositories._
import commons.repositories.DateTimeProvider
import slick.dbio.DBIO
import users.models.UserId
import users.repositories.UserRepo

import scala.concurrent.ExecutionContext

class CommentService(articleRepo: ArticleRepo,
                     userRepo: UserRepo,
                     commentRepo: CommentRepo,
                     dateTimeProvider: DateTimeProvider,
                     commentWithAuthorRepo: CommentWithAuthorRepo,
                     implicit private val ex: ExecutionContext) {

  def delete(id: CommentId, userId: UserId): DBIO[Unit] = {
    require(userId != null)

    for {
      _ <- validateAuthor(id, userId)
      _ <- commentRepo.delete(id)
    } yield ()
  }

  private def validateAuthor(id: CommentId, userId: UserId) = {
    for {
      comment <- commentRepo.findById(id)
      _ <-
        if (userId == comment.authorId) DBIO.successful(())
        else DBIO.failed(new AuthorMismatchException(userId, comment.id))
    } yield ()
  }

  def findByArticleSlug(slug: String, maybeUserId: Option[UserId]): DBIO[Seq[CommentWithAuthor]] = {
    require(slug != null && maybeUserId != null)

    commentWithAuthorRepo.findByArticleSlug(slug, maybeUserId)
  }

  def create(newComment: NewComment, slug: String, userId: UserId): DBIO[CommentWithAuthor] = {
    require(newComment != null && slug != null && userId != null)

    for {
      comment <- doCreate(newComment, slug, userId)
      commentWithAuthor <- commentWithAuthorRepo.getCommentWithAuthor(comment, userId)
    } yield commentWithAuthor
  }

  private def doCreate(newComment: NewComment, slug: String, userId: UserId) = {
    for {
      article <- articleRepo.findBySlug(slug)
      now = dateTimeProvider.now
      comment = Comment(CommentId(-1), article.id, userId, newComment.body, now, now)
      savedComment <- commentRepo.insertAndGet(comment)
    } yield savedComment
  }

}