package articles.models

import play.api.libs.json.{Format, Json}

case class TagListWrapper(tags: Seq[String])

object TagListWrapper {
  implicit val tagListWrapperFormat: Format[TagListWrapper] = Json.format[TagListWrapper]
}