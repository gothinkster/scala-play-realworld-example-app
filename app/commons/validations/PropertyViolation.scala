package commons.validations

import commons.validations.constraints.Violation

case class PropertyViolation(property: String, violation: Violation)