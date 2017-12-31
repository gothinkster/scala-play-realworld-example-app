package core.articles.services

import commons.models.Email
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import core.articles.exceptions.{AuthorMismatchException, MissingArticleException, MissingCommentException}
import core.articles.models._
import core.articles.repositories._
import core.users.models.{User, UserId}
import core.users.repositories.UserRepo
import org.apache.commons.lang3.StringUtils
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class CommentService(articleRepo: ArticleRepo,
                     userRepo: UserRepo,
                     commentRepo: CommentRepo,
                     dateTimeProvider: DateTimeProvider,
                     implicit private val ex: ExecutionContext) {

  private def validateAuthor(user: User, authorId: UserId): DBIO[Unit] = {
    val userId = user.id

    if (userId == authorId) DBIO.successful(())
    else DBIO.failed(new AuthorMismatchException(userId, authorId))
  }

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

  def byArticleSlug(slug: String): DBIO[Seq[CommentWithAuthor]] = {
    require(StringUtils.isNotBlank(slug))

    for {
      maybeArticle <- articleRepo.bySlug(slug)
      article <- DbioUtils.optionToDbio(maybeArticle, new MissingArticleException(slug))
      commentsWithAuthors <- commentRepo.byArticleIdWithAuthor(article.id)
    } yield commentsWithAuthors.map(commentWithAuthor => {
      val (comment, author) = commentWithAuthor

      CommentWithAuthor(comment, author)
    })
  }


  def create(newComment: NewComment, slug: String, email: Email): DBIO[CommentWithAuthor] = {
    require(newComment != null && StringUtils.isNotBlank(slug) && email != null)

    for {
      maybeArticle <- articleRepo.bySlug(slug)
      article <- DbioUtils.optionToDbio(maybeArticle, new MissingArticleException(slug))
      author <- userRepo.byEmail(email)
      comment <- persistComment(newComment, article.id, author.id)
    } yield CommentWithAuthor(comment, author)
  }

  private def persistComment(newComment: NewComment, articleId: ArticleId, authorId: UserId) = {
    val now = dateTimeProvider.now
    val comment = Comment(CommentId(-1), articleId, authorId, newComment.body, now, now)
    commentRepo.create(comment)
  }

}