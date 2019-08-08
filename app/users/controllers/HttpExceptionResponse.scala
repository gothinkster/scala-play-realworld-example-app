package users.controllers

import authentication.exceptions.AuthenticationExceptionCode
import play.api.libs.json.{Format, Json}

private[controllers] case class HttpExceptionResponse(code: AuthenticationExceptionCode) {
  val message: String = code.message
}

private[controllers] object HttpExceptionResponse {
  implicit val jsonWrites: Format[HttpExceptionResponse] = Json.format[HttpExceptionResponse]
}