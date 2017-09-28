package core.authentication.api

import play.api.mvc.{ActionBuilder, AnyContent}

trait AuthenticatedActionBuilder extends ActionBuilder[AuthenticatedUserRequest, AnyContent]
