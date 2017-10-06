package core.users.services

import commons.models.Login
import commons.validations.constraints.{LoginAlreadyTakenViolation, NotNullViolation, Violation}
import commons.validations.{Failure, Success, ValidationResult}
import core.authentication.api.SecurityUserProvider

import scala.concurrent.{ExecutionContext, Future}

private[users] class LoginValidator(securityUserProvider: SecurityUserProvider,
                                    implicit private val ec: ExecutionContext) {

  def validate(login: Login): Future[ValidationResult[Violation]] = {
    if (login == null) Future.successful(Failure(Seq(NotNullViolation)))
    else {
      securityUserProvider.byLogin(login)
        .map(maybeSecurityUser => {
          if (maybeSecurityUser.isDefined) Failure(Seq(LoginAlreadyTakenViolation(login)))
          else Success
        })
    }
  }

}