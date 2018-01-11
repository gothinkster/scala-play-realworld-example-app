package core.users.services

import commons.exceptions.ValidationException
import commons.validations.PropertyViolation
import core.users.models.{User, UserUpdate}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class UserUpdateValidator(usernameValidator: UsernameValidator,
                          emailValidator: EmailValidator,
                          passwordValidator: PasswordValidator,
                          implicit private val ec: ExecutionContext) {

  def validate(user: User, userUpdate: UserUpdate): DBIO[Unit] = {
    val passwordViolations = validatePassword(userUpdate)

    for {
      usernameViolations <- validateUsername(user, userUpdate)
      usernameEmail <- validateEmail(user, userUpdate)
      violations = passwordViolations ++ usernameViolations ++ usernameEmail
      _ <- failIfViolated(violations)
    } yield ()
  }

  private def failIfViolated(violations: Seq[PropertyViolation]) = {
    if (violations.isEmpty) DBIO.successful(())
    else DBIO.failed(new ValidationException(violations))
  }

  private def validatePassword(userUpdate: UserUpdate) = {
    userUpdate.newPassword
      .map(newPassword => passwordValidator.validate(newPassword))
      .getOrElse(Seq.empty)
      .map(violation => PropertyViolation("newPassword", violation))
  }

  private def validateEmail(user: User, userUpdate: UserUpdate) = {
    val newEmail = userUpdate.email
    val violationsAction =
      if (user.email != newEmail) emailValidator.validate(newEmail)
      else DBIO.successful(Seq.empty)

    violationsAction.map(_.map(violation => PropertyViolation("email", violation)))
  }

  private def validateUsername(user: User, userUpdate: UserUpdate) = {
    val newUsername = userUpdate.username
    val violationsAction =
      if (user.username != newUsername) usernameValidator.validate(newUsername)
      else DBIO.successful(Seq.empty)

    violationsAction.map(_.map(violation => PropertyViolation("username", violation)))
  }

}
