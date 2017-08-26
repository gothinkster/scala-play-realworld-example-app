package commons.validations

import commons.validations.constraints.Constraint

sealed abstract class ValidationResult

case class Failure(violatedConstraints: Seq[Constraint]) extends ValidationResult

case object Success extends ValidationResult