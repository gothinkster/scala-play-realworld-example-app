package core.authentication.api

import commons.validations.ValidationResult

trait PasswordValidator {
  def validate(password: PlainTextPassword): ValidationResult
}