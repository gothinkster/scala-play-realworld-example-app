package core.authentication.api

import commons.models.Login

import scala.concurrent.Future

trait SecurityUserProvider {
  def byLogin(login: Login): Future[Option[SecurityUser]]
}