package core.users.services

import commons.utils.RealWorldStringUtils
import commons.validations.constraints._
import core.authentication.api.PlainTextPassword

private[users] class PasswordValidator {
  private val minPassLength = 8
  private val maxPassLength = 255

  def validate(password: PlainTextPassword): Seq[Violation] = {
    if (password == null) Seq(NotNullViolation)
    else {
      val rawPassword = password.value

      if (rawPassword.length < minPassLength) Seq(MinLengthViolation(minPassLength))
      else if (rawPassword.length > maxPassLength) Seq(MaxLengthViolation(maxPassLength))
      else if (RealWorldStringUtils.startsWithWhiteSpace(rawPassword)
        || RealWorldStringUtils.endsWithWhiteSpace(rawPassword)) Seq(PrefixOrSuffixWithWhiteSpacesViolation)
      else Nil
    }
  }
}