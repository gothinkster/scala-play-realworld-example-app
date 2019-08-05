package users.controllers

import play.api.mvc.{ActionBuilder, AnyContent}

trait AuthenticatedActionBuilder extends ActionBuilder[AuthenticatedUserRequest, AnyContent]
