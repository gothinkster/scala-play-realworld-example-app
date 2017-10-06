package core.users.services

import commons.validations.constraints.Violation
import commons.validations.{PropertyViolation, ValidationResult}
import core.users.models.UserRegistration

import scala.concurrent.{ExecutionContext, Future}

private[users] class UserRegistrationValidator(passwordValidator: PasswordValidator,
                                               loginValidator: LoginValidator,
                                               implicit private val ex: ExecutionContext) {
  def validate(userRegistration: UserRegistration): Future[ValidationResult[PropertyViolation]] = {
    val passwordViolations: ValidationResult[Violation] = passwordValidator.validate(userRegistration.password)

    val passwordPropertyValidations = passwordViolations.map(violation => {
      PropertyViolation("password", violation)
    })

    val violations = loginValidator.validate(userRegistration.username)
      .map(violations => violations.map(violation => PropertyViolation("username", violation)))
      .map(loginViolations => loginViolations.zip(passwordPropertyValidations))

    violations
  }
}



