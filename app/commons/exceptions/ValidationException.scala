package commons.exceptions

import commons.validations.PropertyViolation

class ValidationException(val violations: Seq[PropertyViolation])
  extends RuntimeException(violations.toString) {

}
