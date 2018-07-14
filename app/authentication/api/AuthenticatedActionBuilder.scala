package authentication.api

import authentication.models.AuthenticatedUserRequest
import play.api.mvc.{ActionBuilder, AnyContent}

trait AuthenticatedActionBuilder extends ActionBuilder[AuthenticatedUserRequest, AnyContent]
