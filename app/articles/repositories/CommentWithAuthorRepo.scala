
package articles.repositories

import commons.models._
import articles.models._
import users.models.{Profile, UserId}
import users.repositories.ProfileRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class CommentWithAuthorRepo(articleRepo: ArticleRepo,
                            commentRepo: CommentRepo,
                            profileRepo: ProfileRepo,
                            implicit private val ex: ExecutionContext) {

  def findByArticleSlug(slug: String, maybeCurrentUserEmail: Option[Email]): DBIO[Seq[CommentWithAuthor]] = {
    require(slug != null && maybeCurrentUserEmail != null)

    for {
      article <- articleRepo.findBySlug(slug)
      comments <- commentRepo.findByArticleId(article.id)
      commentsWithAuthors <- getCommentsWithAuthors(comments, maybeCurrentUserEmail)
    } yield commentsWithAuthors
  }

  def getCommentWithAuthor(comment: Comment, currentUserEmail: Email): DBIO[CommentWithAuthor] = {
    require(comment != null && currentUserEmail != null)

    getCommentsWithAuthors(Seq(comment), Some(currentUserEmail))
      .map(_.head)
  }

  private def getCommentsWithAuthors(comments: Seq[Comment], maybeCurrentUserEmail: Option[Email]) = {
    val authorIds = comments.map(_.authorId)
    profileRepo.getProfileByUserId(authorIds, maybeCurrentUserEmail)
      .map(profileByUserId => {
        comments.map(comment => getCommentWithAuthor(profileByUserId, comment))
      })
  }

  private def getCommentWithAuthor(profileByUserId: Map[UserId, Profile], comment: Comment) = {
    val profile = profileByUserId(comment.authorId)
    CommentWithAuthor(comment, profile)
  }

}