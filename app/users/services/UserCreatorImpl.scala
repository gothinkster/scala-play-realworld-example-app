package users.services

import javax.inject.Inject
import slick.dbio.DBIO
import users.models.User
import users.repositories.UserRepo
import users.services.api.UserCreator

private[users] class UserCreatorImpl(userRepo: UserRepo) extends UserCreator {

  def create(user: User): DBIO[User] = {
    require(user != null)

    userRepo.create(user)
  }
}