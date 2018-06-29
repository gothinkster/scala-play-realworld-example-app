package core.authentication.api

import play.api.mvc.Request

trait OptionallyAuthenticatedUserRequest[+BodContentType] extends Request[BodContentType] {

  def authenticatedUserOption: Option[AuthenticatedUser]

}
