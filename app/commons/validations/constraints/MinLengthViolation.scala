package commons.validations.constraints

case class MinLengthViolation(minLength: Int) extends Violation {
  override val message: String = s"is too short (minimum is $minLength characters)"
}