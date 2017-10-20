package core.authentication.api

import java.time.LocalDateTime

case class JwtToken(token: String, expiredAt: LocalDateTime) extends AuthenticationResult