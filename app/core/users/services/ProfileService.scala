package core.users.services

import commons.models.Username
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import core.authentication.api._
import core.users.models.Profile
import core.users.repositories.UserRepo
import core.users.services.exceptions.MissingUserException
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[users] class ProfileService(userRepo: UserRepo,
                                    securityUserProvider: SecurityUserProvider,
                                    securityUserUpdater: SecurityUserUpdater,
                                    dateTimeProvider: DateTimeProvider,
                                    userUpdateValidator: UserUpdateValidator,
                                    implicit private val ec: ExecutionContext) {

  def byUsername(username: Username): DBIO[Profile] = {
    require(username != null)

    for {
      maybeUser <- userRepo.byUsername(username)
      user <- DbioUtils.optionToDbio(maybeUser, new MissingUserException(username))
    } yield Profile(user.username, user.bio, user.image, false)
  }

}