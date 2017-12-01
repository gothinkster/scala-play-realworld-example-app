package commons.models

import play.api.libs.json._

case class Email(value: String) extends AnyVal

object Email {
  implicit val emailFormat: Format[Email] = new Format[Email] {
    override def reads(json: JsValue): JsResult[Email] = Reads.StringReads.reads(json).map(Email(_))

    override def writes(o: Email): JsValue = Writes.StringWrites.writes(o.value)
  }
}