package commons.models

import julienrf.json.derived
import play.api.libs.json.Format
import play.api.libs.json._

sealed abstract class ExceptionCode {
  val message: String = ""
}

object ExceptionCode {
  implicit val jsonFormat: Format[ExceptionCode] = derived.flat.oformat((__ \ "type").format[String])
}

case object MissingOrInvalidCredentialsCode extends ExceptionCode {
  override val message: String = "Provide valid Jwt token through Http header Authorization"
}

case object ExpiredCredentialsCode extends ExceptionCode

case object UserDoesNotExistCode extends ExceptionCode