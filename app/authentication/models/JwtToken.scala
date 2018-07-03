package authentication.models

import java.time.Instant

case class JwtToken(token: String, expiredAt: Instant)