package authentication.jwt.services

import java.time.Duration
import java.util.Date

import authentication.api.TokenGenerator
import authentication.models.{IdProfile, JwtToken}
import commons.repositories.DateTimeProvider
import io.jsonwebtoken.{Jwts, SignatureAlgorithm}

private[authentication] class JwtTokenGenerator(dateTimeProvider: DateTimeProvider, secretProvider: SecretProvider)
  extends TokenGenerator[IdProfile, JwtToken] {

  private val tokenDuration = Duration.ofHours(1)

  override def generate(profile: IdProfile): JwtToken = {
    val signedToken = Jwts.builder
      .setExpiration(Date.from(expiredAt))
      .claim(JwtTokenGenerator.securityUserIdClaimName, profile.securityUserId.value.toString)
      .signWith(SignatureAlgorithm.HS256, secretProvider.get)
      .compact()

    JwtToken(signedToken)
  }

  private def expiredAt = {
    val now = dateTimeProvider.now
    now.plus(tokenDuration)
  }
}

private[authentication] object JwtTokenGenerator {
  val securityUserIdClaimName: String = "security_user_id"
}