package core.users.services

import commons.models.Email
import commons.validations.constraints._
import core.authentication.api.SecurityUserProvider
import org.apache.commons.validator.routines.EmailValidator

import scala.concurrent.ExecutionContext

private[users] class EmailValidator(securityUserProvider: SecurityUserProvider,
                                    implicit private val ec: ExecutionContext) {

  def validate(email: Email): Seq[Violation] = {
    if (email == null) Seq(NotNullViolation)
    else {
      val rawEmail = email.value

      if (EmailValidator.getInstance().isValid(rawEmail)) Nil
      else Seq(InvalidEmailViolation(rawEmail))
    }
  }

}