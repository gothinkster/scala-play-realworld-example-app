package users.controllers

import play.api.mvc.Request
import play.api.mvc.Security.AuthenticatedRequest
import users.authentication.AuthenticatedUser

class AuthenticatedUserRequest[+A](authenticatedUser: AuthenticatedUser, request: Request[A])
  extends AuthenticatedRequest[A, AuthenticatedUser](authenticatedUser, request)
    with OptionallyAuthenticatedUserRequest[A] {

  override val authenticatedUserOption: Option[AuthenticatedUser] = Some(authenticatedUser)

}
