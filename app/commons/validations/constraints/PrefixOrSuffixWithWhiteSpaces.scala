package commons.validations.constraints

case object PrefixOrSuffixWithWhiteSpaces extends Violation {
  override def message: String = "Remove redundant whitespace characters"
}
