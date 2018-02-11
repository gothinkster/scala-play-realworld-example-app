package commons.validations.constraints

case object PrefixOrSuffixWithWhiteSpacesViolation extends Violation {
  override def message: String = "remove redundant whitespace characters"
}
