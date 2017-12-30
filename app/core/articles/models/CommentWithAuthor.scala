
package core.articles.models

import java.time.Instant

import core.users.models.User
import play.api.libs.json._

case class CommentWithAuthor(id: CommentId,
                             articleId: ArticleId,
                             author: User,
                             body: String,
                             createdAt: Instant,
                             updatedAt: Instant
                            )

object CommentWithAuthor {
  implicit val commentFormat: Format[CommentWithAuthor] = Json.format[CommentWithAuthor]

  def apply(comment: Comment, author: User): CommentWithAuthor = CommentWithAuthor(
    comment.id,
    comment.articleId,
    author,
    comment.body,
    comment.createdAt,
    comment.updatedAt
  )
}