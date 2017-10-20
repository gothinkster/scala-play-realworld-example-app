package authentication.services

import java.time.{Duration, LocalDateTime}

import commons.models.Username
import commons.repositories.DateTimeProvider
import commons.utils.DateUtils
import core.authentication.api.{JwtToken, RealWorldAuthenticator, UsernameProfile}
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.jwt.JwtClaims
import org.pac4j.jwt.profile.{JwtGenerator, JwtProfile}

private[authentication] class Pack4jJwtAuthenticator(dateTimeProvider: DateTimeProvider,
                                                     jwtGenerator: JwtGenerator[CommonProfile])
  extends RealWorldAuthenticator[UsernameProfile, JwtToken] {

  private val tokenDuration = Duration.ofHours(12)

  override def authenticate(profile: UsernameProfile): JwtToken = {
    val expiredAt = dateTimeProvider.now.plus(tokenDuration)
    val username = profile.username

    val rawToken = jwtGenerator.generate(buildProfile(username, expiredAt))

    JwtToken(rawToken, expiredAt)
  }

  private def buildProfile(username: Username, expiredAt: LocalDateTime) = {
    val profile = new JwtProfile()
    profile.setId(username.value)
    profile.addAttribute(JwtClaims.EXPIRATION_TIME, DateUtils.toOldJavaDate(expiredAt))

    profile
  }

}