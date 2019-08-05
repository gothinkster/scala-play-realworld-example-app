
package articles.repositories

import articles.models._
import slick.dbio.DBIO
import users.models.{Profile, UserId}
import users.repositories.ProfileRepo

import scala.concurrent.ExecutionContext

class CommentWithAuthorRepo(articleRepo: ArticleRepo,
                            commentRepo: CommentRepo,
                            profileRepo: ProfileRepo,
                            implicit private val ex: ExecutionContext) {

  def findByArticleSlug(slug: String, maybeUserId: Option[UserId]): DBIO[Seq[CommentWithAuthor]] = {
    require(slug != null && maybeUserId != null)

    for {
      article <- articleRepo.findBySlug(slug)
      comments <- commentRepo.findByArticleId(article.id)
      commentsWithAuthors <- getCommentsWithAuthors(comments, maybeUserId)
    } yield commentsWithAuthors
  }

  def getCommentWithAuthor(comment: Comment, userId: UserId): DBIO[CommentWithAuthor] = {
    require(comment != null && userId != null)

    getCommentsWithAuthors(Seq(comment), Some(userId))
      .map(_.head)
  }

  private def getCommentsWithAuthors(comments: Seq[Comment], maybeUserId: Option[UserId]) = {
    val authorIds = comments.map(_.authorId)
    profileRepo.getProfileByUserId(authorIds, maybeUserId)
      .map(profileByUserId => {
        comments.map(comment => getCommentWithAuthor(profileByUserId, comment))
      })
  }

  private def getCommentWithAuthor(profileByUserId: Map[UserId, Profile], comment: Comment) = {
    val profile = profileByUserId(comment.authorId)
    CommentWithAuthor(comment, profile)
  }

}