package authentication

import commons.models.{Email, ExceptionCode, ExpiredCredentialsCode, MissingOrInvalidCredentialsCode}
import commons.repositories.DateTimeProvider
import commons.utils.DateUtils
import core.authentication.api.AuthenticatedUser
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.HeaderClient
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.jwt.profile.JwtProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc.RequestHeader
import play.mvc.Http

abstract class AbstractPack4jAuthenticatedActionBuilder(sessionStore: PlaySessionStore,
                                                        dateTimeProvider: DateTimeProvider,
                                                        jwtAuthenticator: JwtAuthenticator) {

  private val prefixSpaceIsCrucialHere = "Token "
  private val client = new HeaderClient(Http.HeaderNames.AUTHORIZATION, prefixSpaceIsCrucialHere, jwtAuthenticator)

  protected def authenticate(requestHeader: RequestHeader): Either[ExceptionCode, AuthenticatedUser] = {
    val webContext = new PlayWebContext(requestHeader, sessionStore)

    Option(client.getCredentials(webContext))
      .toRight(MissingOrInvalidCredentialsCode)
      .map(client.getUserProfile(_, webContext))
      .filterOrElse(isNotExpired, ExpiredCredentialsCode)
      .map(profile => AuthenticatedUser(Email(profile.getId)))
  }

  private def isNotExpired(profile: CommonProfile): Boolean =
    profile.isInstanceOf[JwtProfile] && isNotExpired(profile.asInstanceOf[JwtProfile])

  private def isNotExpired(profile: JwtProfile) = {
    val expirationDate = profile.getExpirationDate
    val expiredAt = DateUtils.toInstant(expirationDate)

    dateTimeProvider.now.isBefore(expiredAt)
  }

}
