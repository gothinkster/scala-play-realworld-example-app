package users.services

import javax.inject.Inject
import commons.models.Login
import users.models.User
import users.repositories.UserRepo
import users.services.api.UserProvider
import slick.dbio.DBIO

private[users] class UserProviderImpl(userRepo: UserRepo) extends UserProvider {

  override def byLogin(login: Login): DBIO[Option[User]] = {
    require(login != null)

    userRepo.byLogin(login)
  }

}