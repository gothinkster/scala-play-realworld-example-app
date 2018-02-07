package core.users.services

import commons.exceptions.ValidationException
import commons.repositories.DateTimeProvider
import commons.validations.PropertyViolation
import core.authentication.api.{NewSecurityUser, SecurityUserCreator}
import core.users.models.{User, UserId, UserRegistration}
import core.users.repositories.UserRepo
import play.api.Configuration
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext.Implicits.global

private[users] class UserRegistrationService(userRegistrationValidator: UserRegistrationValidator,
                                             securityUserCreator: SecurityUserCreator,
                                             dateTimeProvider: DateTimeProvider,
                                             userRepo: UserRepo,
                                             config: Configuration) {

  private val defaultImage = Some(config.get[String]("app.defaultImage"))

  def register(userRegistration: UserRegistration): DBIO[User] = {
    for {
      violations <- userRegistrationValidator.validate(userRegistration)
      _ <- failIfViolated(violations)
      user <- doRegister(userRegistration)
    } yield user
  }

  private def failIfViolated(violations: Seq[PropertyViolation]) = {
    if (violations.isEmpty) DBIO.successful(())
    else DBIO.failed(new ValidationException(violations))
  }

  private def doRegister(userRegistration: UserRegistration) = {
    val newSecurityUser = NewSecurityUser(userRegistration.email, userRegistration.password)
    for {
      _ <- securityUserCreator.create(newSecurityUser)
      now = dateTimeProvider.now
      user = User(UserId(-1), userRegistration.username, userRegistration.email, null, defaultImage, now, now)
      savedUser <- userRepo.insertAndGet(user)
    } yield savedUser
  }
}



