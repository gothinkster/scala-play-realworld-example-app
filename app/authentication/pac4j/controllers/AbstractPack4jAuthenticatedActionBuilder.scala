package authentication.pac4j.controllers

import java.time.Instant
import java.util.Date

import authentication.exceptions.ExceptionWithCode
import authentication.repositories.SecurityUserRepo
import commons.models._
import commons.repositories.DateTimeProvider
import commons.services.ActionRunner
import commons.utils.DbioUtils
import core.authentication.api._
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http.client.direct.HeaderClient
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.jwt.profile.JwtProfile
import org.pac4j.play.PlayWebContext
import org.pac4j.play.store.PlaySessionStore
import play.api.mvc.RequestHeader
import play.mvc.Http
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[authentication] abstract class AbstractPack4jAuthenticatedActionBuilder(sessionStore: PlaySessionStore,
                                                                                dateTimeProvider: DateTimeProvider,
                                                                                jwtAuthenticator: JwtAuthenticator,
                                                                                actionRunner: ActionRunner,
                                                                                securityUserRepo: SecurityUserRepo)
                                                                               (implicit ec: ExecutionContext)
  extends OptionallyAuthenticatedActionBuilder {

  private val prefixSpaceIsCrucialHere = "Token "
  private val client = new HeaderClient(Http.HeaderNames.AUTHORIZATION, prefixSpaceIsCrucialHere, jwtAuthenticator)

  protected def authenticate(requestHeader: RequestHeader): DBIO[(Email, String)] = {
    val webContext = new PlayWebContext(requestHeader, sessionStore)
    val credentials = client.getCredentials(webContext)
    Option(credentials)
      .toRight(MissingOrInvalidCredentialsCode)
      .map(client.getUserProfile(_, webContext))
      .filterOrElse(isNotExpired, ExpiredCredentialsCode)
      .fold(exceptionCode => DBIO.failed(new ExceptionWithCode(exceptionCode)), profile => DBIO.successful(profile))
      .map(profile => mapToSecurityUserId(profile))
      .flatMap(existsSecurityUser)
      .map(email => (email, credentials.getToken))
  }

  private def mapToSecurityUserId(profile: CommonProfile) = {
    SecurityUserId(java.lang.Long.parseLong(profile.getId))
  }

  private def existsSecurityUser(securityUserId: SecurityUserId) = {
    securityUserRepo.findByIdOption(securityUserId)
      .flatMap(maybeSecurityUser => DbioUtils.optionToDbio(maybeSecurityUser,
        new ExceptionWithCode(UserDoesNotExistCode)))
      .map(securityUser => securityUser.email)
  }

  private def isNotExpired(profile: CommonProfile): Boolean =
    profile.isInstanceOf[JwtProfile] && isNotExpired(profile.asInstanceOf[JwtProfile])

  private def isNotExpired(profile: JwtProfile) = {
    val expirationDate = profile.getExpirationDate
    val expiredAt = toInstant(expirationDate)

    dateTimeProvider.now.isBefore(expiredAt)
  }

  private def toInstant(date: Date): Instant = {
    if (date == null) null
    else date.toInstant
  }

}
