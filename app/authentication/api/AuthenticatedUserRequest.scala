package authentication.api

import authentication.models.AuthenticatedUser
import play.api.mvc.Request
import play.api.mvc.Security.AuthenticatedRequest

class AuthenticatedUserRequest[A](authenticatedUser: AuthenticatedUser, request: Request[A])
  extends AuthenticatedRequest[A, AuthenticatedUser](authenticatedUser, request)
