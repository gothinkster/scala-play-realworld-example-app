package core.authentication.api

import scala.concurrent.Future

trait SecurityUserCreator {
  def create(newSecUser: NewSecurityUser): Future[SecurityUser]
}