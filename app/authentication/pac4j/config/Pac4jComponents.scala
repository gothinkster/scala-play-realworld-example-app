package authentication.pac4j.config

import authentication.api.AuthenticatedActionBuilder
import authentication.repositories.SecurityUserRepo
import authentication.{HttpBasicAuthenticator, Pack4jAuthenticatedActionBuilder}
import com.softwaremill.macwire.wire
import commons.config.WithExecutionContext
import commons.repositories.ActionRunner
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.profile.CommonProfile
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import org.pac4j.jwt.profile.JwtGenerator
import org.pac4j.play.store.{PlayCacheSessionStore, PlaySessionStore}
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.mvc.PlayBodyParsers
import play.cache.DefaultAsyncCacheApi

trait Pac4jComponents extends WithExecutionContext {

  def actionRunner: ActionRunner
  def securityUserRepo: SecurityUserRepo
  lazy val usernamePasswordAuthenticator: Authenticator[UsernamePasswordCredentials] = wire[HttpBasicAuthenticator]

  def configuration: Configuration
  private val secret: String = configuration.get[String]("play.http.secret.key")

  private lazy val signatureConfig = new SecretSignatureConfiguration(secret)
  protected lazy val jwtGenerator: JwtGenerator[CommonProfile] = new JwtGenerator(signatureConfig)
  lazy val jwtAuthenticator: JwtAuthenticator = new JwtAuthenticator(signatureConfig)

  def playBodyParsers: PlayBodyParsers
  lazy val authenticatedAction: AuthenticatedActionBuilder = wire[Pack4jAuthenticatedActionBuilder]

  def defaultCacheApi: AsyncCacheApi
  protected lazy val sessionStore: PlaySessionStore = {
    val defaultAsyncCacheApi = new DefaultAsyncCacheApi(defaultCacheApi)
    val syncCacheApi: play.cache.SyncCacheApi = new play.cache.DefaultSyncCacheApi(defaultAsyncCacheApi)

    new PlayCacheSessionStore(syncCacheApi)
  }
}
