package commons.models

import play.api.libs.json.{Format, Json}

case class ValidationResultWrapper(errors: Map[String, Seq[String]])

object ValidationResultWrapper {
  implicit val validationResultWrapperFormat: Format[ValidationResultWrapper] = Json.format[ValidationResultWrapper]
}