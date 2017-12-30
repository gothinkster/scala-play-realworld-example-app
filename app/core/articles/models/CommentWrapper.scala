
package core.articles.models

import play.api.libs.json.{Format, Json}

case class CommentWrapper(comment: CommentWithAuthor)

object CommentWrapper {
  implicit val commentWrapperJsonFormat: Format[CommentWrapper] = Json.format[CommentWrapper]
}