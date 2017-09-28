package core.authentication.api

import play.api.libs.json._

case class BearerTokenResponse(token: String) {
  val aType: String = "Bearer"
}

// todo
object BearerTokenResponse {
  implicit val jsonReads: Reads[BearerTokenResponse] = Json.reads[BearerTokenResponse]
  implicit val jsonWrites: Writes[BearerTokenResponse] = (tokenResponse: BearerTokenResponse) => {
    JsObject(List(
      "token" -> JsString(tokenResponse.token),
      "type" -> JsString(tokenResponse.aType)
    ))
  }
}