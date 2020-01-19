package commons.models

import play.api.libs.json._

import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class Username(value: String) extends AnyVal

object Username {
  implicit val usernameFormat: Format[Username] = new Format[Username] {
    override def reads(json: JsValue): JsResult[Username] = Reads.StringReads.reads(json).map(Username(_))

    override def writes(o: Username): JsValue = Writes.StringWrites.writes(o.value)
  }

  implicit val usernameDbMapping: BaseColumnType[Username] = MappedColumnType.base[Username, String](
    vo => vo.value,
    username => Username(username)
  )
}