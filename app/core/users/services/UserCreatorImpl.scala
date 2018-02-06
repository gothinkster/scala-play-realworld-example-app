package core.users.services

import core.users.models.User
import core.users.repositories.UserRepo
import core.users.services.api.UserCreator
import slick.dbio.DBIO

private[users] class UserCreatorImpl(userRepo: UserRepo) extends UserCreator {

  def create(user: User): DBIO[User] = {
    require(user != null)

    userRepo.insertAndGet(user)
  }
}