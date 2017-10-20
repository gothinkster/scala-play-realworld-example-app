package core.users.services

import commons.models.Email
import commons.validations.constraints._
import core.authentication.api.SecurityUserProvider

import scala.concurrent.{ExecutionContext, Future}

private[users] class EmailValidator(securityUserProvider: SecurityUserProvider,
                                    implicit private val ec: ExecutionContext) {

  private val emailValidator = org.apache.commons.validator.routines.EmailValidator.getInstance()

  def validate(email: Email): Future[Seq[Violation]] = {
    if (email == null) Future.successful(Seq(NotNullViolation))
    else if (isNotEmailValid(email)) Future.successful(Seq(InvalidEmailViolation(email)))
    else validEmailAlreadyTaken(email)
  }

  private def validEmailAlreadyTaken(email: Email) = {
    securityUserProvider.byEmail(email)
      .map(maybeSecurityUser => {
        if (maybeSecurityUser.isDefined) Seq(EmailAlreadyTakenViolation(email))
        else Nil
      })
  }

  private def isNotEmailValid(email: Email) = {
    !emailValidator.isValid(email.value)
  }
}