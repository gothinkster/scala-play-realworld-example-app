package users.services

import authentication.api._
import authentication.models.SecurityUserId
import commons.exceptions.ValidationException
import commons.models.Email
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import slick.dbio.DBIO
import users.models._
import users.repositories.UserRepo

import scala.concurrent.ExecutionContext

private[users] class UserService(userRepo: UserRepo,
                                 securityUserProvider: SecurityUserProvider,
                                 securityUserUpdater: SecurityUserUpdater,
                                 dateTimeProvider: DateTimeProvider,
                                 userUpdateValidator: UserUpdateValidator,
                                 implicit private val ec: ExecutionContext) {

  def getUserDetails(userId: UserId): DBIO[UserDetails] = {
    require(userId != null)

    userRepo.findById(userId)
      .map(UserDetails(_))
  }
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

  private def updateSecurityUser(securityUserId: SecurityUserId, userUpdate: UserUpdate) = {
    securityUserUpdater.update(securityUserId, SecurityUserUpdate(userUpdate.email, userUpdate.password))
  }

  def update(userId: UserId, userUpdate: UserUpdate): DBIO[UserDetails] = {
    require(userId != null && userUpdate != null)

    for {
      user <- userRepo.findById(userId)
      _ <- validate(userUpdate, user)
      updatedUser <- updateUser(user, userUpdate)
      _ <- updateSecurityUser(user.securityUserId, userUpdate)
    } yield UserDetails(updatedUser)
  }

  private def validate(userUpdate: UserUpdate, user: User) = {
    userUpdateValidator.validate(userUpdate, user)
      .flatMap(violations => DbioUtils.fail(violations.isEmpty, new ValidationException(violations)))
  }

}
