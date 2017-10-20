package commons.validations.constraints

import commons.models.Email

case class EmailAlreadyTakenViolation(email: Email) extends Violation {
  override val message: String = s" - ${email.value} is already taken"
}