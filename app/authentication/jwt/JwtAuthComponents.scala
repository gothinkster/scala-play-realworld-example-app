package authentication.jwt

import authentication.api._
import authentication.jwt.services.{JwtAuthenticator, JwtTokenGenerator, SecretProvider}
import authentication.models.{IdProfile, JwtToken}
import com.softwaremill.macwire.wire
import commons.CommonsComponents
import commons.config.WithExecutionContextComponents
import play.api.Configuration
import play.api.mvc.PlayBodyParsers

private[authentication] trait JwtAuthComponents extends WithExecutionContextComponents with CommonsComponents {

  def configuration: Configuration

  def playBodyParsers: PlayBodyParsers

  def securityUserProvider: SecurityUserProvider

  private lazy val secretProvider = new SecretProvider(configuration)
  lazy val jwtAuthenticator: JwtAuthenticator = wire[JwtAuthenticator]

  lazy val jwtTokenGenerator: TokenGenerator[IdProfile, JwtToken] = wire[JwtTokenGenerator]
}