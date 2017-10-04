package core.authentication.api

import commons.validations.ValidationResult
import commons.validations.constraints.Violation

trait PasswordValidator {
  def validate(password: PlainTextPassword): ValidationResult[Violation]
}