package core.authentication.api

import play.api.mvc.{ActionBuilder, AnyContent}

trait OptionallyAuthenticatedActionBuilder extends ActionBuilder[MaybeAuthenticatedUserRequest, AnyContent]