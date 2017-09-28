package core.users.services.api

import commons.models.Login
import slick.dbio.DBIO
import core.users.models.User

trait UserProvider {
  def byLogin(login: Login): DBIO[Option[User]]
}
