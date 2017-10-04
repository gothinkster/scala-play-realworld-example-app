package commons.validations

import commons.validations.constraints.Violation

sealed abstract class ValidationResult[+ViolationType] {
  def map[AnotherViolationType](f: ViolationType => AnotherViolationType): ValidationResult[AnotherViolationType]
}

case class PropertyViolation(property: String, violation: Violation)

case class Failure[ViolationType](violations: Seq[ViolationType]) extends ValidationResult[ViolationType] {
  override def map[AnotherViolationType](f: (ViolationType) => AnotherViolationType)
  : ValidationResult[AnotherViolationType] = {
    Failure(violations.map(f))
  }
}

case object Success extends ValidationResult[Nothing] {
  override def map[AnotherViolationType](f: (Nothing) => AnotherViolationType)
  : ValidationResult[AnotherViolationType] = {
    this
  }
}