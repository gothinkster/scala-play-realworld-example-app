package users.services

import javax.inject.Inject

import authentication.services.api.PasswordValidator
import commons.validations.ValidationResult
import users.models.UserRegistration

private[users] class UserRegistrationValidator(passwordValidator: PasswordValidator) {
  def validate(userRegistration: UserRegistration): ValidationResult = {
    passwordValidator.validate(userRegistration.password)
  }
}



