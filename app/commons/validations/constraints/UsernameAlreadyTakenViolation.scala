package commons.validations.constraints

import commons.models.Username

case class UsernameAlreadyTakenViolation(username: Username) extends Violation {
  override val message: String = s" - ${username.value} is already taken"
}