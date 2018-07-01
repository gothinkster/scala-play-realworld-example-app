package authentication.api

import commons.models.Email
import play.api.libs.json._

case class NewSecurityUser(email: Email, password: PlainTextPassword)

object NewSecurityUser {
  implicit val newSecurityUserFormat: Format[NewSecurityUser] = Json.format[NewSecurityUser]
}

case class PlainTextPassword(value: String) extends AnyVal

object PlainTextPassword {
  implicit val plainTextPasswordFormat: Format[PlainTextPassword] = new Format[PlainTextPassword] {
    override def reads(json: JsValue): JsResult[PlainTextPassword] =
      Reads.StringReads.reads(json).map(PlainTextPassword(_))

    override def writes(o: PlainTextPassword): JsValue = Writes.StringWrites.writes(o.value)
  }
}