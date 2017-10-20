package core.users.services.api

import commons.models.Username
import slick.dbio.DBIO
import core.users.models.User

trait UserProvider {
  def byUsername(username: Username): DBIO[Option[User]]
}
