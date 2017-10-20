package core.users.services

import commons.repositories.ActionRunner
import commons.validations.PropertyViolation
import core.users.models.UserRegistration
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[users] class UserRegistrationValidator(passwordValidator: PasswordValidator,
                                               loginValidator: LoginValidator,
                                               emailValidator: EmailValidator,
                                               actionRunner: ActionRunner,
                                               implicit private val ex: ExecutionContext) {
  def validate(userRegistration: UserRegistration): DBIO[Seq[PropertyViolation]] = {
    val passwordViolations: Seq[PropertyViolation] = passwordValidator.validate(userRegistration.password)
      .map(violation => PropertyViolation("password", violation))

    val loginViolationsDbio: DBIO[Seq[PropertyViolation]] = loginValidator.validate(userRegistration.username)
      .map(violations => violations.map(violation => PropertyViolation("username", violation)))

    val emailViolationsFuture: DBIO[Seq[PropertyViolation]] = emailValidator.validate(userRegistration.email)
      .map(violations => violations.map(violation => PropertyViolation("email", violation)))

    loginViolationsDbio
      .zip(emailViolationsFuture)
      .map(pair => {
        val (loginViolations, emailViolations) = pair

        loginViolations ++ emailViolations ++ passwordViolations
      })
  }
}



