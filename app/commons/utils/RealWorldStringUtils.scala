package commons.utils

object RealWorldStringUtils {
  def startsWithWhiteSpace(str: String): Boolean = str.matches("(?U)^\\s.*")

  def endsWithWhiteSpace(str: String): Boolean = str.matches("(?U).*\\s$")
}
