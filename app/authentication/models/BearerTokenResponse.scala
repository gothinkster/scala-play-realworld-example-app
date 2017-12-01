package authentication.models

import java.time.Instant

import play.api.libs.json.{Format, Json}

case class BearerTokenResponse(token: String, expiredAt: Instant) {
  val aType: String = "Bearer"
}

object BearerTokenResponse {
  implicit val bearerTokenResponseFormat: Format[BearerTokenResponse] = Json.format[BearerTokenResponse]
}