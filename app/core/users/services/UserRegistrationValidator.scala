package core.users.services

import commons.validations.constraints.Violation
import commons.validations.{PropertyViolation, ValidationResult}
import core.authentication.api.PasswordValidator
import core.users.models.UserRegistration

private[users] class UserRegistrationValidator(passwordValidator: PasswordValidator) {
  def validate(userRegistration: UserRegistration): ValidationResult[PropertyViolation] = {
    val passwordViolations: ValidationResult[Violation] = passwordValidator.validate(userRegistration.password)

    passwordViolations.map(violation => {
      PropertyViolation("password", violation)
    })
  }
}



