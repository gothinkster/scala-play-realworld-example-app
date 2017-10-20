package core.authentication.api

import commons.models.Email

import scala.concurrent.Future

trait SecurityUserProvider {
  def byEmail(email: Email): Future[Option[SecurityUser]]
}