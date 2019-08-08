package users.controllers

import play.api.mvc.{ActionBuilder, AnyContent}

trait OptionallyAuthenticatedActionBuilder extends ActionBuilder[OptionallyAuthenticatedUserRequest, AnyContent]
