package commons.validations

import commons.validations.constraints.Violation

sealed abstract class ValidationResult[+ViolationType] {
  def zip[OtherValidationType >: ViolationType](violations: ValidationResult[OtherValidationType])
  : ValidationResult[OtherValidationType]

  def map[AnotherViolationType](f: ViolationType => AnotherViolationType): ValidationResult[AnotherViolationType]
}

case class PropertyViolation(property: String, violation: Violation)

case class Failure[ViolationType](violations: Seq[ViolationType]) extends ValidationResult[ViolationType] {
  override def map[AnotherViolationType](f: (ViolationType) => AnotherViolationType)
  : ValidationResult[AnotherViolationType] = {
    Failure(violations.map(f))
  }

  override def zip[OtherValidationType >: ViolationType](validationResult: ValidationResult[OtherValidationType])
  : ValidationResult[OtherValidationType] = {
    validationResult match {
      case Failure(otherViolations) => Failure(violations ++ otherViolations)
      case Success => this
    }
  }
}

case object Success extends ValidationResult[Nothing] {
  override def map[AnotherViolationType](f: (Nothing) => AnotherViolationType)
  : ValidationResult[AnotherViolationType] = {
    this
  }

  override def zip[OtherValidationType >: Nothing](violations: ValidationResult[OtherValidationType])
  : ValidationResult[OtherValidationType] = violations
}