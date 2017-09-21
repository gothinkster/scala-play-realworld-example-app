package authentication.models

import commons.models.ExceptionCode
import play.api.libs.json.{Format, Json}

case class HttpExceptionResponse(code: ExceptionCode) {
  val message: String = code.message
}

object HttpExceptionResponse {
  implicit val jsonWrites: Format[HttpExceptionResponse] = Json.format[HttpExceptionResponse]
}