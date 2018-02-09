package core.authentication.api

import play.api.mvc.Request
import slick.dbio.DBIO

trait Authenticator[RequestBodyType] {
  def authenticate(request: Request[RequestBodyType]): DBIO[String]
}