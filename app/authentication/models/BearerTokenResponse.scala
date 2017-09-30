package authentication.models

import java.time.LocalDateTime

case class BearerTokenResponse(token: String, expiredAt: LocalDateTime) {
  val aType: String = "Bearer"
}