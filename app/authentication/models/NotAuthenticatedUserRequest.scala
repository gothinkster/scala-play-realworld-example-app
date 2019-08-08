package authentication.models

import play.api.mvc.{Request, WrappedRequest}
import users.authentication.AuthenticatedUser
import users.controllers.OptionallyAuthenticatedUserRequest

class NotAuthenticatedUserRequest[+A](request: Request[A])
  extends WrappedRequest[A](request) with OptionallyAuthenticatedUserRequest[A] {

  override def authenticatedUserOption: Option[AuthenticatedUser] = None

}