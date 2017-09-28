package core.users.services.api

import slick.dbio.DBIO
import core.users.models.User

trait UserCreator {
  def create(user: User): DBIO[User]
}
