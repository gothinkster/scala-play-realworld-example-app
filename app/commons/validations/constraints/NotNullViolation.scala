package commons.validations.constraints

case object NotNullViolation extends Violation {
  override def message: String = "can not be empty"
}
