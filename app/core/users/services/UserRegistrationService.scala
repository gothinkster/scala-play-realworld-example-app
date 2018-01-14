package core.users.services

import commons.exceptions.ValidationException
import commons.repositories.DateTimeProvider
import core.authentication.api.{NewSecurityUser, SecurityUserCreator}
import core.users.models.{User, UserId, UserRegistration}
import core.users.services.api.UserCreator
import play.api.Configuration
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext.Implicits.global

private[users] class UserRegistrationService(userRegistrationValidator: UserRegistrationValidator,
                                             securityUserCreator: SecurityUserCreator,
                                             userCreator: UserCreator,
                                             dateTimeProvider: DateTimeProvider,
                                             config: Configuration) {

  private val defaultImage = Some(config.get[String]("app.defaultImage"))

  def register(userRegistration: UserRegistration): DBIO[User] = {
    userRegistrationValidator.validate(userRegistration)
      .flatMap(violations =>
        if (violations.isEmpty) doRegister(userRegistration)
        else DBIO.failed(new ValidationException(violations))
      )
  }

  private def doRegister(userRegistration: UserRegistration) = {
    val now = dateTimeProvider.now
    val newSecurityUser = NewSecurityUser(userRegistration.email, userRegistration.password)
    securityUserCreator.create(newSecurityUser)
      .zip(userCreator.create(User(UserId(-1), userRegistration.username, userRegistration.email, null, defaultImage,
        now, now)))
      .map(_._2)
  }
}



