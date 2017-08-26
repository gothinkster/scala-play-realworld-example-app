package authentication.oauth2

import java.time.Duration

private[authentication] class AuthenticationConfig {
  val clientId = "play-with-food"
  val scope = "play-with-food"
  val redirectUri = "play-with-food-authenticated"

  val usernameParamName = "username"
  val passwordParamName = "password"

  val accessTokenLifeDuration: Duration = Duration.ofHours(24)
}