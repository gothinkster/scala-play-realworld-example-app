package core.users.services

import commons.exceptions.ValidationException
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import core.authentication.api.{NewSecurityUser, SecurityUserCreator, SecurityUserId}
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

  def register(userRegistration: UserRegistration): DBIO[(User, SecurityUserId)] = {
    for {
      violations <- userRegistrationValidator.validate(userRegistration)
      _ <- DbioUtils.fail(violations.isEmpty, new ValidationException(violations))
      userAndSecurityUserId <- doRegister(userRegistration)
    } yield userAndSecurityUserId
  }

  private def doRegister(userRegistration: UserRegistration) = {
    val newSecurityUser = NewSecurityUser(userRegistration.email, userRegistration.password)
    for {
      securityUser <- securityUserCreator.create(newSecurityUser)
      now = dateTimeProvider.now
      user = User(UserId(-1), userRegistration.username, userRegistration.email, null, defaultImage, now, now)
      savedUser <- userRepo.insertAndGet(user)
    } yield (savedUser, securityUser.id)
  }
}



