package authentication.services.api

import authentication.models.SecurityUser
import commons.models.Login
import slick.dbio.DBIO

trait SecurityUserProvider {
  def byLogin(login: Login): DBIO[Option[SecurityUser]]
}