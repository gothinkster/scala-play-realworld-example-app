package authentication.services.api

import authentication.models.api.PlainTextPassword
import commons.validations.ValidationResult

trait PasswordValidator {
  def validate(password: PlainTextPassword): ValidationResult
}