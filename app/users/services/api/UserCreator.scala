package users.services.api

import slick.dbio.DBIO
import users.models.User

trait UserCreator {
  def create(user: User): DBIO[User]
}
