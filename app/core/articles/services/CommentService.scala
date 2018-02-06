package core.articles.services

import commons.models.Email
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import core.articles.exceptions.{AuthorMismatchException, MissingArticleException, MissingCommentException}
import core.articles.models._
import core.articles.repositories._
import core.users.models.{User, UserId}
import core.users.repositories.{ProfileRepo, UserRepo}
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
      user <- userRepo.byEmail(email)
      maybeComment <- commentRepo.byId(id)
      comment <- DbioUtils.optionToDbio(maybeComment, new MissingCommentException(id))
      _ <- validateAuthor(user, comment.authorId)
      _ <- commentRepo.delete(id)
    } yield ()
  }

  private def validateAuthor(user: User, authorId: UserId): DBIO[Unit] = {
    val userId = user.id

    if (userId == authorId) DBIO.successful(())
    else DBIO.failed(new AuthorMismatchException(userId, authorId))
  }

  def byArticleSlug(slug: String, maybeCurrentUserEmail: Option[Email]): DBIO[Seq[CommentWithAuthor]] = {
    require(slug != null && maybeCurrentUserEmail != null)

    commentWithAuthorRepo.byArticleSlug(slug, maybeCurrentUserEmail)
  }

  def create(newComment: NewComment, slug: String, currentUserEmail: Email): DBIO[CommentWithAuthor] = {
    require(newComment != null && slug != null && currentUserEmail != null)

    for {
      maybeArticle <- articleRepo.bySlug(slug)
      article <- DbioUtils.optionToDbio(maybeArticle, new MissingArticleException(slug))
      author <- userRepo.byEmail(currentUserEmail)
      comment <- doCreate(newComment, article.id, author.id)
      commentWithAuthor <- commentWithAuthorRepo.getCommentWithAuthor(comment, author, currentUserEmail)
    } yield commentWithAuthor
  }

  private def doCreate(newComment: NewComment, articleId: ArticleId, authorId: UserId) = {
    val now = dateTimeProvider.now
    val comment = Comment(CommentId(-1), articleId, authorId, newComment.body, now, now)
    commentRepo.insertAndGet(comment)
  }

}