package users.controllers

import play.api.mvc.Request
import users.authentication.AuthenticatedUser

trait OptionallyAuthenticatedUserRequest[+BodyContentType] extends Request[BodyContentType] {

  def authenticatedUserOption: Option[AuthenticatedUser]

}
