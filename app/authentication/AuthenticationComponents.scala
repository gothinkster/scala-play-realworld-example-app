package authentication

import com.softwaremill.macwire.wire
import commons.config.{WithControllerComponents, WithExecutionContextComponents}
import authentication.api.{Authenticator, SecurityUserCreator, SecurityUserProvider, SecurityUserUpdater}
import authentication.jwt.JwtAuthComponents
import authentication.models.CredentialsWrapper
import authentication.repositories.SecurityUserRepo
import authentication.services.{SecurityUserService, UsernameAndPasswordAuthenticator}
import commons.CommonsComponents

trait AuthenticationComponents extends CommonsComponents
  with WithControllerComponents
  with WithExecutionContextComponents
  with JwtAuthComponents {

  lazy val securityUserCreator: SecurityUserCreator = wire[SecurityUserService]
  lazy val securityUserProvider: SecurityUserProvider = wire[SecurityUserService]
  lazy val securityUserUpdater: SecurityUserUpdater = wire[SecurityUserService]
  lazy val securityUserRepo: SecurityUserRepo = wire[SecurityUserRepo]

  lazy val usernamePasswordAuthenticator: Authenticator[CredentialsWrapper] = wire[UsernameAndPasswordAuthenticator]
}
