package core.users.services

import javax.inject.Inject
import slick.dbio.DBIO
import core.users.models.User
import core.users.repositories.UserRepo
import core.users.services.api.UserCreator

private[users] class UserCreatorImpl(userRepo: UserRepo) extends UserCreator {

  def create(user: User): DBIO[User] = {
    require(user != null)

    userRepo.create(user)
  }
}