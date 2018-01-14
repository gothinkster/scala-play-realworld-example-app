package core.users.services

import commons.repositories.ActionRunner
import commons.validations.PropertyViolation
import core.users.models.UserRegistration
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[users] class UserRegistrationValidator(passwordValidator: PasswordValidator,
                                               usernameValidator: UsernameValidator,
                                               emailValidator: EmailValidator,
                                               actionRunner: ActionRunner,
                                               implicit private val ex: ExecutionContext) {

  def validate(userRegistration: UserRegistration): DBIO[Seq[PropertyViolation]] = {
    for {
      usernameViolations <- validateUsername(userRegistration)
      emailViolations <- validateEmail(userRegistration)
      passwordViolations = validatePassword(userRegistration)
    } yield passwordViolations ++ usernameViolations ++ emailViolations
  }

  private def validatePassword(userRegistration: UserRegistration) = {
    passwordValidator.validate(userRegistration.password)
      .map(violation => PropertyViolation("password", violation))
  }

  private def validateEmail(userRegistration: UserRegistration) = {
    emailValidator.validate(userRegistration.email)
      .map(violations => violations.map(violation => PropertyViolation("email", violation)))
  }

  private def validateUsername(userRegistration: UserRegistration) = {
    usernameValidator.validate(userRegistration.username)
      .map(violations => violations.map(violation => PropertyViolation("username", violation)))
  }
}



