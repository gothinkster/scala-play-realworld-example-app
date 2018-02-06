package commons.validations.constraints

case object PrefixOrSuffixWithWhiteSpacesViolation extends Violation {
  override def message: String = "Remove redundant whitespace characters"
}
