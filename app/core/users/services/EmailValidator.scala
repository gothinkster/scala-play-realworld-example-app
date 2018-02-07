package core.users.services

import commons.models.Email
import commons.validations.constraints._
import core.authentication.api.SecurityUserProvider
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[users] class EmailValidator(securityUserProvider: SecurityUserProvider,
                                    implicit private val ec: ExecutionContext) {

  private val emailValidator = org.apache.commons.validator.routines.EmailValidator.getInstance()

  def validate(email: Email): DBIO[Seq[Violation]] = {
    if (email == null) DBIO.successful(Seq(NotNullViolation))
    else if (isNotEmailValid(email)) DBIO.successful(Seq(InvalidEmailViolation(email)))
    else validEmailAlreadyTaken(email)
  }

  private def validEmailAlreadyTaken(email: Email) = {
    securityUserProvider.findByEmail(email)
      .map(maybeSecurityUser => {
        if (maybeSecurityUser.isDefined) Seq(EmailAlreadyTakenViolation(email))
        else Nil
      })
  }

  private def isNotEmailValid(email: Email) = {
    !emailValidator.isValid(email.value)
  }
}