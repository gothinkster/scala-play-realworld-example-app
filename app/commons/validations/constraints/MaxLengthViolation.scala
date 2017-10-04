package commons.validations.constraints

case class MaxLengthViolation(maxPassLength: Int) extends Violation {
  override val message: String = s"is too short (minimum is $maxPassLength characters)"
}
