package commons.validations.constraints

case class MaxLengthViolation(maxLength: Int) extends Violation {
  override val message: String = s"is too long (maximum is $maxLength characters)"
}
