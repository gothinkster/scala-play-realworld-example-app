package articles.models

import play.api.libs.json.{Format, Json}

case class CommentList(comments: Seq[CommentWithAuthor])

object CommentList {
  implicit val commentListFormat: Format[CommentList] = Json.format[CommentList]
}