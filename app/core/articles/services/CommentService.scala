package core.articles.services

import commons.models.Email
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import core.articles.exceptions.MissingArticleException
import core.articles.models._
import core.articles.repositories._
import core.users.models.UserId
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class CommentService(articleRepo: ArticleRepo,
                     articleTagRepo: ArticleTagRepo,
                     tagRepo: TagRepo,
                     userRepo: UserRepo,
                     commentRepo: CommentRepo,
                     dateTimeProvider: DateTimeProvider,
                     articleWithTagsRepo: ArticleWithTagsRepo,
                     implicit private val ex: ExecutionContext) {

  def create(newComment: NewComment, slug: String, email: Email): DBIO[CommentWithAuthor] = {
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