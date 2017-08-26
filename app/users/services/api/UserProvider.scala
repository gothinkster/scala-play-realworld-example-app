package users.services.api

import commons.models.Login
import slick.dbio.DBIO
import users.models.User

trait UserProvider {
  def byLogin(login: Login): DBIO[Option[User]]
}
