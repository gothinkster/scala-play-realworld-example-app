package commons.validations.constraints

case class MinLengthViolation(minPasswordLength: Int) extends Violation {
  override val message: String = s"is too short (minimum is $minPasswordLength characters)"
}
