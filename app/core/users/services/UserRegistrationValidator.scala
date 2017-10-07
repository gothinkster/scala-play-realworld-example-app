package core.users.services

import commons.validations.PropertyViolation
import core.users.models.UserRegistration

import scala.concurrent.{ExecutionContext, Future}

private[users] class UserRegistrationValidator(passwordValidator: PasswordValidator,
                                               loginValidator: LoginValidator,
                                               emailValidator: EmailValidator,
                                               implicit private val ex: ExecutionContext) {
  def validate(userRegistration: UserRegistration): Future[Seq[PropertyViolation]] = {
    val passwordViolations = passwordValidator.validate(userRegistration.password)
      .map(violation => PropertyViolation("password", violation))

    val loginViolationsFuture = loginValidator.validate(userRegistration.username)
      .map(violations => violations.map(violation => PropertyViolation("username", violation)))

    val emailViolations = emailValidator.validate(userRegistration.email)
      .map(violation => PropertyViolation("email", violation))

    loginViolationsFuture
      .map(loginViolations => loginViolations ++ passwordViolations ++ emailViolations)
  }
}



