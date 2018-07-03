package authentication.exceptions

import julienrf.json.derived
import play.api.libs.json.{Format, __}

sealed trait AuthenticationExceptionCode {
  def message: String = ""
}

object AuthenticationExceptionCode {
  implicit val jsonFormat: Format[AuthenticationExceptionCode] = derived.flat.oformat((__ \ "type").format[String])
}

case object MissingOrInvalidCredentialsCode extends AuthenticationExceptionCode {
  override val message: String = "Provide valid Jwt token through Http header Authorization"
}

case object ExpiredCredentialsCode extends AuthenticationExceptionCode

case object UserDoesNotExistCode extends AuthenticationExceptionCode