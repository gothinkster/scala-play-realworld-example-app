package core.authentication.api

import java.time.Instant

case class JwtToken(token: String, expiredAt: Instant) extends AuthenticationResult