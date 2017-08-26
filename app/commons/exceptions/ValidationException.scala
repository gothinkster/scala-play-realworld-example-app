package commons.exceptions

import commons.validations.constraints.Constraint

class ValidationException(val violatedConstraints: Seq[Constraint])
  extends RuntimeException(violatedConstraints.toString()) {

}
