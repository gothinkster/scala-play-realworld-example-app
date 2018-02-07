package commons.models

trait BaseId[U] extends Any {
  def value: U
}
