package commons.validations.constraints

case class InvalidEmailViolation(email: String) extends Violation {
  override val message: String = s" - $email is invalid"
}