package commons.controllers.mappings

import commons.models.{Email, Username}
import core.commons.models.ValidationResultWrapper
import play.api.libs.json._

trait CommonJsonMappings {
  implicit val usernameReads: Reads[Username] = Reads((Reads.StringReads.reads(_)).andThen(_.map(str => Username(str))))
  implicit val usernameWrites: Writes[Username] = Writes((Writes.StringWrites.writes(_)).compose(_.value))

  implicit val usernameFormat: Format[Username] = new Format[Username] {
    override def reads(json: JsValue): JsResult[Username] = usernameReads.reads(json)

    override def writes(o: Username): JsValue = usernameWrites.writes(o)
  }

  implicit val emailReads: Reads[Email] = Reads((Reads.StringReads.reads(_)).andThen(_.map(str => Email(str))))
  implicit val emailWrites: Writes[Email] = Writes((Writes.StringWrites.writes(_)).compose(_.value))

  implicit val emailFormat: Format[Email] = new Format[Email] {
    override def reads(json: JsValue): JsResult[Email] = emailReads.reads(json)

    override def writes(o: Email): JsValue = emailWrites.writes(o)
  }

  implicit val validationResultWrapperFormat: Format[ValidationResultWrapper] = Json.format[ValidationResultWrapper]

}