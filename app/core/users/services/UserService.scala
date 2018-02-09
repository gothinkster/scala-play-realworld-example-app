package core.users.services

import commons.exceptions.ValidationException
import commons.models.Email
import commons.repositories.DateTimeProvider
import commons.validations.PropertyViolation
import core.authentication.api._
import core.users.models._
import core.users.repositories.UserRepo
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
    for {
      Some(securityUser) <- securityUserProvider.findByEmail(currentEmail)
      withUpdatedEmail <- maybeUpdateEmail(securityUser, userUpdate)
      _ <- maybeUpdatePassword(withUpdatedEmail, userUpdate)
    } yield ()
  }

  private def maybeUpdatePassword(securityUser: SecurityUser, userUpdate: UserUpdate) = {
    userUpdate.password
      .map(newPassword => securityUserUpdater.updatePassword(securityUser, newPassword))
      .getOrElse(DBIO.successful(securityUser))
  }

  private def maybeUpdateEmail(securityUser: SecurityUser, userUpdate: UserUpdate) = {
    userUpdate.email
      .filter(_ != securityUser.email)
      .map(newEmail => securityUserUpdater.updateEmail(securityUser, newEmail))
      .getOrElse(DBIO.successful(securityUser))
  }

  def update(currentEmail: Email, userUpdate: UserUpdate): DBIO[UserDetails] = {
    require(currentEmail != null && userUpdate != null)

    for {
      user <- userRepo.findByEmail(currentEmail)
      violations <- userUpdateValidator.validate(user, userUpdate)
      _ <- failIfViolated(violations)
      updatedUser <- updateUser(user, userUpdate)
      _ <- updateSecurityUser(currentEmail, userUpdate)
    } yield UserDetails(updatedUser)
  }

  private def failIfViolated(violations: Seq[PropertyViolation]) = {
    if (violations.isEmpty) DBIO.successful(())
    else DBIO.failed(new ValidationException(violations))
  }
}