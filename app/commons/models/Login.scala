package commons.models

import play.api.libs.json.{Reads, Writes}

case class Login(value: String) extends AnyVal

object Login {
  implicit val loginReads: Reads[Login] = Reads((Reads.StringReads.reads(_)).andThen(_.map(str => Login(str))))
  implicit val loginWrites: Writes[Login] = Writes((Writes.StringWrites.writes(_)).compose(_.value))
}