package authentication.controllers.mappings

import java.time.LocalDateTime

import authentication.models.BearerTokenResponse
import play.api.libs.json._

trait AuthenticationJsonMappings extends  {

  implicit val bearerTokenResponseFormat: Format[BearerTokenResponse] = new Format[BearerTokenResponse] {
    override def writes(o: BearerTokenResponse): JsValue = {
      JsObject(List(
        "token" -> JsString(o.token),
        "type" -> JsString(o.aType),
        "expiredAt" -> implicitly[Writes[LocalDateTime]].writes(o.expiredAt)
      ))
    }

    override def reads(json: JsValue): JsResult[BearerTokenResponse] = Json.reads[BearerTokenResponse].reads(json)
  }
}