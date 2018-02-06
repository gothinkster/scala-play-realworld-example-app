package authentication.services

import java.time.{Duration, Instant}
import java.util.Date

import commons.repositories.DateTimeProvider
import core.authentication.api.{EmailProfile, JwtToken, RealWorldAuthenticator}
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.jwt.JwtClaims
import org.pac4j.jwt.profile.{JwtGenerator, JwtProfile}

private[authentication] class Pack4jJwtAuthenticator(dateTimeProvider: DateTimeProvider,
                                                     jwtGenerator: JwtGenerator[CommonProfile])
  extends RealWorldAuthenticator[EmailProfile, JwtToken] {

  private val tokenDuration = Duration.ofHours(12)

  override def authenticate(profile: EmailProfile): JwtToken = {
    val expiredAt = dateTimeProvider.now.plus(tokenDuration)
    val username = profile.email

    val rawToken = jwtGenerator.generate(buildProfile(username, expiredAt))

    JwtToken(rawToken, expiredAt)
  }

  private def buildProfile(email: String, expiredAt: Instant) = {
    val profile = new JwtProfile()
    profile.setId(email)
    profile.addAttribute(JwtClaims.EXPIRATION_TIME, toOldJavaDate(expiredAt))

    profile
  }

  private def toOldJavaDate(instant: Instant): Date = {
    if (instant == null) null
    else Date.from(instant)
  }

}