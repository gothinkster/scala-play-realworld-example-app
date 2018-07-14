
package articles.models

import play.api.libs.json._

case class NewComment(body: String)

object NewComment {
  implicit val newCommentFormat: Format[NewComment] = Json.format[NewComment]
}