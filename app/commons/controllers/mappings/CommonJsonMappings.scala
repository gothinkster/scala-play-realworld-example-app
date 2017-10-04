package commons.controllers.mappings

import commons.models.{Email, Login}
import core.commons.models.ValidationResultWrapper
import play.api.libs.json._

trait CommonJsonMappings {
  implicit val loginReads: Reads[Login] = Reads((Reads.StringReads.reads(_)).andThen(_.map(str => Login(str))))
  implicit val loginWrites: Writes[Login] = Writes((Writes.StringWrites.writes(_)).compose(_.value))

  implicit val loginFormat: Format[Login] = new Format[Login] {
    override def reads(json: JsValue): JsResult[Login] = loginReads.reads(json)

    override def writes(o: Login): JsValue = loginWrites.writes(o)
  }

  implicit val emailReads: Reads[Email] = Reads((Reads.StringReads.reads(_)).andThen(_.map(str => Email(str))))
  implicit val emailWrites: Writes[Email] = Writes((Writes.StringWrites.writes(_)).compose(_.value))

  implicit val emailFormat: Format[Email] = new Format[Email] {
    override def reads(json: JsValue): JsResult[Email] = emailReads.reads(json)

    override def writes(o: Email): JsValue = emailWrites.writes(o)
  }

  implicit val validationResultWrapperFormat: Format[ValidationResultWrapper] = Json.format[ValidationResultWrapper]

}