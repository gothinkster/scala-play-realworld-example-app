package authentication.oauth2

import scalaoauth2.provider.{Implicit, OAuthGrantType, TokenEndpoint}

private[authentication] class PlayWithFoodTokenEndpoint extends TokenEndpoint {
  override val handlers = Map(
    OAuthGrantType.IMPLICIT -> new Implicit()
  )
}
