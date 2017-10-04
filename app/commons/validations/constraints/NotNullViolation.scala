package commons.validations.constraints

case object NotNullViolation extends Violation {
  override def message: String = "Can not be empty"
}
