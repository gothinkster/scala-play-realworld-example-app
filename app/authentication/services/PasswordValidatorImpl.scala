package authentication.services

import authentication.models.api.PlainTextPassword
import authentication.services.api.PasswordValidator
import commons.utils.PwfStringUtils
import commons.validations.constraints._
import commons.validations.{Failure, Success, ValidationResult}

private[authentication] class PasswordValidatorImpl extends PasswordValidator {
  private val minPassLength = 12
  private val maxPassLength = 255

  override def validate(password: PlainTextPassword): ValidationResult = Option(password) match {
    case None => Failure(Seq(NotNullConstraint))
    case Some(PlainTextPassword(pass)) if pass.length < minPassLength =>
      Failure(Seq(MinLengthConstraint(minPassLength)))
    case Some(PlainTextPassword(pass)) if pass.length > maxPassLength =>
      Failure(Seq(MaxLengthConstraint(maxPassLength)))
    case Some(PlainTextPassword(pass)) if PwfStringUtils.startsWithWhiteSpace(pass) ||
      PwfStringUtils.endsWithWhiteSpace(pass) => Failure(Seq(PrefixOrSuffixWithWhiteSpaces))
    case _ => Success
  }

}