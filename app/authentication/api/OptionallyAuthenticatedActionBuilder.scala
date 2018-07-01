package authentication.api

import play.api.mvc.{ActionBuilder, AnyContent}

trait OptionallyAuthenticatedActionBuilder extends ActionBuilder[OptionallyAuthenticatedUserRequest, AnyContent]