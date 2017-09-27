package commons.controllers.mappings

import commons.models.Login
import play.api.libs.json._

trait LoginJsonMappings {
  implicit val loginReads: Reads[Login] = Reads((Reads.StringReads.reads(_)).andThen(_.map(str => Login(str))))
  implicit val loginWrites: Writes[Login] = Writes((Writes.StringWrites.writes(_)).compose(_.value))

  implicit val loginFormat: Format[Login] = new Format[Login] {
    override def reads(json: JsValue): JsResult[Login] = loginReads.reads(json)

    override def writes(o: Login): JsValue = loginWrites.writes(o)
  }
}