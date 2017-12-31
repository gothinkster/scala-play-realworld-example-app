package commons.models

import play.api.libs.json._
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class Email(value: String) extends AnyVal

object Email {
  implicit val emailFormat: Format[Email] = new Format[Email] {
    override def reads(json: JsValue): JsResult[Email] = Reads.StringReads.reads(json).map(Email(_))

    override def writes(o: Email): JsValue = Writes.StringWrites.writes(o.value)
  }

  implicit val emailDbMapping: BaseColumnType[Email] = MappedColumnType.base[Email, String](
    vo => vo.value,
    email => Email(email)
  )

}