package authentication.jwt.services

import authentication.exceptions._
import authentication.models.SecurityUserId
import commons.repositories.DateTimeProvider
import io.jsonwebtoken._
import play.api.mvc.RequestHeader
import play.mvc.Http

class JwtAuthenticator(dateTimeProvider: DateTimeProvider,
                                secretProvider: SecretProvider) {

  def authenticate(requestHeader: RequestHeader): Either[AuthenticationExceptionCode, (SecurityUserId, String)] = {
    requestHeader.headers.get(Http.HeaderNames.AUTHORIZATION)
      .map(parse)
      .toRight(MissingOrInvalidCredentialsCode)
      .flatMap(validate)
  }

  private def expirationIsMissing(jwt: Jws[Claims]): Boolean = {
    jwt.getBody.getExpiration == null
  }

  private def validate(rawToken: String) = {
    try {
      val jwt = Jwts.parser()
        .setSigningKey(secretProvider.get)
        .parseClaimsJws(rawToken)

      if (expirationIsMissing(jwt)) Left(MissingOrInvalidCredentialsCode)
      else if (isNotExpired(jwt)) Right((getSecurityUserId(jwt), rawToken))
      else Left(ExpiredCredentialsCode)
    } catch {
      case _: JwtException =>
        Left(MissingOrInvalidCredentialsCode)
    }
  }

  private def parse(authorizationHeader: String) = {
    authorizationHeader.stripPrefix("Token ")
  }

  private def getSecurityUserId(jwt: Jws[Claims]) = {
    val securityUserId = java.lang.Long.parseLong(jwt.getBody.get(JwtTokenGenerator.securityUserIdClaimName, classOf[String]))
    SecurityUserId(securityUserId)
  }

  private def isNotExpired(jwt: Jws[Claims]) = {
    val expiration = jwt.getBody.getExpiration.toInstant

    dateTimeProvider.now.isBefore(expiration)
  }

}
