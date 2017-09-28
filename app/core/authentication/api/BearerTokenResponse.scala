package core.authentication.api

import play.api.libs.json._

case class BearerTokenResponse(token: String) {
  val aType: String = "Bearer"
}

object BearerTokenResponse {

  implicit val jsonFormat: Format[BearerTokenResponse] = new Format[BearerTokenResponse] {
    override def writes(o: BearerTokenResponse): JsValue = {
      JsObject(List(
        "token" -> JsString(o.token),
        "type" -> JsString(o.aType)
      ))
    }

    override def reads(json: JsValue): JsResult[BearerTokenResponse] = Json.reads[BearerTokenResponse].reads(json)
  }

}