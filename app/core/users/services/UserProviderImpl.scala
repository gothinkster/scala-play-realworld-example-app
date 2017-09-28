package core.users.services

import javax.inject.Inject
import commons.models.Login
import core.users.models.User
import core.users.repositories.UserRepo
import core.users.services.api.UserProvider
import slick.dbio.DBIO

private[users] class UserProviderImpl(userRepo: UserRepo) extends UserProvider {

  override def byLogin(login: Login): DBIO[Option[User]] = {
    require(login != null)

    userRepo.byLogin(login)
  }

}