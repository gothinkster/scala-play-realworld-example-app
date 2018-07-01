package authentication.pac4j.services

import java.time.{Duration, Instant}
import java.util.Date

import commons.repositories.DateTimeProvider
import authentication.api.{JwtToken, TokenGenerator, SecurityUserIdProfile}
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.jwt.JwtClaims
import org.pac4j.jwt.profile.{JwtGenerator, JwtProfile}

private[authentication] class JwtTokenGenerator(dateTimeProvider: DateTimeProvider,
                                                jwtGenerator: JwtGenerator[CommonProfile])
  extends TokenGenerator[SecurityUserIdProfile, JwtToken] {

  private val tokenDuration = Duration.ofHours(12)

  override def generate(profile: SecurityUserIdProfile): JwtToken = {
    val expiredAt = dateTimeProvider.now.plus(tokenDuration)
    val profileId = profile.securityUserId

    val rawToken = jwtGenerator.generate(buildProfile(profileId.value, expiredAt))

    JwtToken(rawToken, expiredAt)
  }

  private def buildProfile(profileId: Any, expiredAt: Instant) = {
    val profile = new JwtProfile()
    profile.setId(profileId)
    profile.addAttribute(JwtClaims.EXPIRATION_TIME, toOldJavaDate(expiredAt))

    profile
  }

  private def toOldJavaDate(instant: Instant): Date = {
    if (instant == null) null
    else Date.from(instant)
  }

}