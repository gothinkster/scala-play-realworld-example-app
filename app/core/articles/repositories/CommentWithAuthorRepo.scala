
package core.articles.repositories

import commons.models._
import commons.utils.DbioUtils
import core.articles.exceptions.MissingArticleException
import core.articles.models._
import core.users.models.{Profile, User, UserId}
import core.users.repositories.ProfileRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class CommentWithAuthorRepo(articleRepo: ArticleRepo,
                            commentRepo: CommentRepo,
                            profileRepo: ProfileRepo,
                            implicit private val ex: ExecutionContext) {

  def byArticleSlug(slug: String, maybeCurrentUserEmail: Option[Email]): DBIO[Seq[CommentWithAuthor]] = {
    require(slug != null && maybeCurrentUserEmail != null)

    for {
      maybeArticle <- articleRepo.bySlug(slug)
      article <- DbioUtils.optionToDbio(maybeArticle, new MissingArticleException(slug))
      commentsAndAuthors <- commentRepo.byArticleIdWithAuthor(article.id)
      commentsWithAuthors <- getCommentsWithAuthors(commentsAndAuthors, maybeCurrentUserEmail)
    } yield commentsWithAuthors
  }

  private def getCommentsWithAuthors(commentsAndAuthors: Seq[(Comment, User)], maybeCurrentUserEmail: Option[Email]) = {
    val (comments, authors) = commentsAndAuthors.unzip
    profileRepo.getProfileByUserId(authors, maybeCurrentUserEmail)
      .map(profileByUserId => doGetCommentsWithAuthors(comments, profileByUserId))
  }

  private def doGetCommentsWithAuthors(comments: Seq[Comment], profileByUserId: Map[UserId, Profile]) = {
    comments.map(comment => getCommentWithAuthor(profileByUserId, comment))
  }

  private def getCommentWithAuthor(profileByUserId: Map[UserId, Profile], comment: Comment) = {
    val profile = profileByUserId(comment.authorId)
    CommentWithAuthor(comment, profile)
  }

  def getCommentWithAuthor(comment: Comment, author: User, currentUserEmail: Email): DBIO[CommentWithAuthor] = {
    getCommentsWithAuthors(Seq((comment, author)), Some(currentUserEmail))
      .map(_.head)
  }

}