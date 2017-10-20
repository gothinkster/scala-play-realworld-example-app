package core.users.services

import commons.models.Username
import core.users.models.User
import core.users.repositories.UserRepo
import core.users.services.api.UserProvider
import slick.dbio.DBIO

private[users] class UserProviderImpl(userRepo: UserRepo) extends UserProvider {

  override def byUsername(username: Username): DBIO[Option[User]] = {
    require(username != null)

    userRepo.byUsername(username)
  }

}