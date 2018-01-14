package core.users.services

import commons.validations.PropertyViolation
import core.users.models.{User, UserUpdate}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class UserUpdateValidator(usernameValidator: UsernameValidator,
                          emailValidator: EmailValidator,
                          passwordValidator: PasswordValidator,
                          implicit private val ec: ExecutionContext) {

  def validate(user: User, userUpdate: UserUpdate): DBIO[Seq[PropertyViolation]] = {
    for {
      usernameViolations <- validateUsername(user, userUpdate)
      usernameEmail <- validateEmail(user, userUpdate)
      passwordViolations = validatePassword(userUpdate)
    } yield passwordViolations ++ usernameViolations ++ usernameEmail
  }

  private def validatePassword(userUpdate: UserUpdate) = {
    userUpdate.password
      .map(newPassword => passwordValidator.validate(newPassword))
      .getOrElse(Seq.empty)
      .map(violation => PropertyViolation("password", violation))
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
