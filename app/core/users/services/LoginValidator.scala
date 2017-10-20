package core.users.services

import commons.models.Login
import commons.validations.constraints._
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[users] class LoginValidator(userRepo: UserRepo,
                                    implicit private val ec: ExecutionContext) {
  private val minPassLength = 3
  private val maxPassLength = 255

  def validate(login: Login): DBIO[Seq[Violation]] = {
    if (login == null) DBIO.successful(Seq(NotNullViolation))
    else if (login.value.length < minPassLength) DBIO.successful(Seq(MinLengthViolation(minPassLength)))
    else if (login.value.length > maxPassLength) DBIO.successful(Seq(MaxLengthViolation(maxPassLength)))
    else validLoginAlreadyTaken(login)
  }

  private def validLoginAlreadyTaken(login: Login) = {
    userRepo.byLogin(login)
      .map(maybeUser => {
        if (maybeUser.isDefined) Seq(LoginAlreadyTakenViolation(login))
        else Nil
      })
  }

}