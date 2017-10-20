package commons.validations.constraints

import commons.models.Email

case class InvalidEmailViolation(email: Email) extends Violation {
  override val message: String = s" - ${email.value} is invalid"
}