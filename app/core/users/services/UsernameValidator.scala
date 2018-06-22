package core.users.services

import commons.models.Username
import commons.validations.constraints._
import core.users.repositories.UserRepo
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[users] class UsernameValidator(userRepo: UserRepo,
                                       implicit private val ec: ExecutionContext) {
  private val minPassLength = 3
  private val maxPassLength = 255

  def validate(username: Username): DBIO[Seq[Violation]] = {
    if (username == null) DBIO.successful(Seq(NotNullViolation))
    else if (username.value.length < minPassLength) DBIO.successful(Seq(MinLengthViolation(minPassLength)))
    else if (username.value.length > maxPassLength) DBIO.successful(Seq(MaxLengthViolation(maxPassLength)))
    else validUsernameAlreadyTaken(username)
  }

  private def validUsernameAlreadyTaken(username: Username) = {
    userRepo.findByUsernameOption(username)
      .map(maybeUser => {
        if (maybeUser.isDefined) Seq(UsernameAlreadyTakenViolation(username))
        else Nil
      })
  }

}