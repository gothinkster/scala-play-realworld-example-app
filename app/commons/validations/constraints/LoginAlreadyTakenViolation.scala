package commons.validations.constraints

import commons.models.Login

case class LoginAlreadyTakenViolation(login: Login) extends Violation {
  override val message: String = s" - ${login.value} is already taken"
}