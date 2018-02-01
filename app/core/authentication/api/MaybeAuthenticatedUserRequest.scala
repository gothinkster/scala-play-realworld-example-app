package core.authentication.api

import play.api.mvc.Request
import play.api.mvc.Security.AuthenticatedRequest

class MaybeAuthenticatedUserRequest[A](authenticatedUser: Option[AuthenticatedUser], request: Request[A])
  extends AuthenticatedRequest[A, Option[AuthenticatedUser]](authenticatedUser, request)