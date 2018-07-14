package authentication.models

import authentication.exceptions.AuthenticationExceptionCode
import play.api.libs.json.{Format, Json}

private[authentication] case class HttpExceptionResponse(code: AuthenticationExceptionCode) {
  val message: String = code.message
}

private[authentication] object HttpExceptionResponse {
  implicit val jsonWrites: Format[HttpExceptionResponse] = Json.format[HttpExceptionResponse]
}