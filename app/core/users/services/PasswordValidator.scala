package core.users.services

import commons.utils.StringUtils
import commons.validations.constraints._
import commons.validations.{Failure, Success, ValidationResult}
import core.authentication.api.PlainTextPassword

private[users] class PasswordValidator {
  private val minPassLength = 8
  private val maxPassLength = 255

  def validate(password: PlainTextPassword): ValidationResult[Violation] = {
    if (password == null) Failure(Seq(NotNullViolation))
    else {
      val rawPassword = password.value

      if (rawPassword.length < minPassLength) Failure(Seq(MinLengthViolation(minPassLength)))
      else if (rawPassword.length > maxPassLength) Failure(Seq(MaxLengthViolation(maxPassLength)))
      else if (StringUtils.startsWithWhiteSpace(rawPassword)) Failure(Seq(PrefixOrSuffixWithWhiteSpaces))
      else Success
    }
  }
}