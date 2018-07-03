package authentication.api

import authentication.models.AuthenticatedUser
import play.api.mvc.Request

trait OptionallyAuthenticatedUserRequest[+BodyContentType] extends Request[BodyContentType] {

  def authenticatedUserOption: Option[AuthenticatedUser]

}
