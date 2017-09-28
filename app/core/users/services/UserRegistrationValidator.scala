package core.users.services

import javax.inject.Inject

import commons.validations.ValidationResult
import core.authentication.api.PasswordValidator
import core.users.models.UserRegistration

private[users] class UserRegistrationValidator(passwordValidator: PasswordValidator) {
  def validate(userRegistration: UserRegistration): ValidationResult = {
    passwordValidator.validate(userRegistration.password)
  }
}



