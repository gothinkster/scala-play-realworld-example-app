
package articles.models

import play.api.libs.json._

case class NewCommentWrapper(comment: NewComment)

object NewCommentWrapper {
  implicit val commentWrapperJsonFormat: Format[NewCommentWrapper] = Json.format[NewCommentWrapper]
}