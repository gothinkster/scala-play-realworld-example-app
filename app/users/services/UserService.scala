package users.services

import commons.exceptions.ValidationException
import commons.models.Email
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import authentication.api._
import authentication.models.SecurityUser
import users.models._
import users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[users] class UserService(userRepo: UserRepo,
                                 securityUserProvider: SecurityUserProvider,
                                 securityUserUpdater: SecurityUserUpdater,
                                 dateTimeProvider: DateTimeProvider,
                                 userUpdateValidator: UserUpdateValidator,
                                 implicit private val ec: ExecutionContext) {

  def getUserDetails(email: Email): DBIO[UserDetails] = {
    require(email != null)

    userRepo.findByEmail(email)
      .map(UserDetails(_))
  }

  private def updateUser(user: User, userUpdate: UserUpdate) = {
    val username = userUpdate.username.getOrElse(user.username)
    val email = userUpdate.email.getOrElse(user.email)
    val image = userUpdate.image.orElse(user.image)
    val bio = userUpdate.bio.orElse(user.bio)
    val updateUser = user.copy(username = username, email = email, bio = bio, image = image,
      updatedAt = dateTimeProvider.now)

    userRepo.updateAndGet(updateUser)
  }

  private def updateSecurityUser(currentEmail: Email, userUpdate: UserUpdate) = {
    securityUserUpdater.update(currentEmail, SecurityUserUpdate(userUpdate.email, userUpdate.password))
  }

  def update(currentEmail: Email, userUpdate: UserUpdate): DBIO[UserDetails] = {
    require(currentEmail != null && userUpdate != null)

    for {
      user <- userRepo.findByEmail(currentEmail)
      _ <- validate(userUpdate, user)
      updatedUser <- updateUser(user, userUpdate)
      _ <- updateSecurityUser(currentEmail, userUpdate)
    } yield UserDetails(updatedUser)
  }

  private def validate(userUpdate: UserUpdate, user: User) = {
    userUpdateValidator.validate(userUpdate, user)
      .flatMap(violations => DbioUtils.fail(violations.isEmpty, new ValidationException(violations)))
  }

}
