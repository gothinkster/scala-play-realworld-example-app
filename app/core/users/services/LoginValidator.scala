package core.users.services

import commons.models.Login
import commons.validations.constraints._
import core.authentication.api.SecurityUserProvider

import scala.concurrent.{ExecutionContext, Future}

private[users] class LoginValidator(securityUserProvider: SecurityUserProvider,
                                    implicit private val ec: ExecutionContext) {
  private val minPassLength = 3
  private val maxPassLength = 255

  def validate(login: Login): Future[Seq[Violation]] = {
    if (login == null) Future.successful(Seq(NotNullViolation))
    else {
      val rawLogin = login.value

      if (rawLogin.length < minPassLength) Future.successful(Seq(MinLengthViolation(minPassLength)))
      else if (rawLogin.length > maxPassLength) Future.successful(Seq(MaxLengthViolation(maxPassLength)))
      else {
        securityUserProvider.byLogin(login)
          .map(maybeSecurityUser => {
            if (maybeSecurityUser.isDefined) Seq(LoginAlreadyTakenViolation(login))
            else Nil
          })
      }
    }
  }

}