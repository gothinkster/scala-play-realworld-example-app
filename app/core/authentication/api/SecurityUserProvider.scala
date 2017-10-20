package core.authentication.api

import commons.models.Email
import slick.dbio.DBIO

trait SecurityUserProvider {
  def byEmail(email: Email): DBIO[Option[SecurityUser]]
}