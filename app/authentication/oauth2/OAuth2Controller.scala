package authentication.oauth2

import play.api.mvc._

import scala.concurrent.ExecutionContext
import scalaoauth2.provider._

class OAuth2Controller(dataHandler: PlayWithFoodAuthorizationHandler,
                       components: ControllerComponents,
                       implicit private val ex: ExecutionContext)
  extends AbstractController(components) with OAuth2Provider {
  override val tokenEndpoint = new PlayWithFoodTokenEndpoint()

  def accessToken: Action[AnyContent] = Action.async { implicit request =>
    issueAccessToken(dataHandler)
  }
}