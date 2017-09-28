package core.authentication.api

import commons.models.Login
import slick.dbio.DBIO

trait SecurityUserProvider {
  def byLogin(login: Login): DBIO[Option[SecurityUser]]
}