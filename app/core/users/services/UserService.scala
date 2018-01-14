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

  private def updateUser(user: User, userUpdate: UserUpdate) = {
    val updateUser = user.copy(username = userUpdate.username, email = userUpdate.email, bio = userUpdate.bio,
      image = userUpdate.image, updatedAt = dateTimeProvider.now)

    userRepo.update(updateUser)
  }

  private def updateSecurityUser(currentEmail: Email, userUpdate: UserUpdate) = {
    for {
      Some(securityUser) <- securityUserProvider.byEmail(currentEmail)
      _ <- maybeUpdateEmail(securityUser, userUpdate)
      _ <- maybeUpdatePassword(securityUser, userUpdate)
    } yield ()
  }

  private def maybeUpdatePassword(securityUser: SecurityUser, userUpdate: UserUpdate) = {
    userUpdate.password
      .map(newPassword => securityUserUpdater.updatePassword(securityUser, newPassword))
      .getOrElse(DBIO.successful(()))
  }

  private def maybeUpdateEmail(securityUser: SecurityUser, userUpdate: UserUpdate) = {
    if (securityUser.email != userUpdate.email) securityUserUpdater.updateEmail(securityUser, userUpdate.email)
    else DBIO.successful(())
  }

  def update(currentEmail: Email, userUpdate: UserUpdate): DBIO[UserDetails] = {
    require(currentEmail != null && userUpdate != null)

    for {
      user <- userRepo.byEmail(currentEmail)
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