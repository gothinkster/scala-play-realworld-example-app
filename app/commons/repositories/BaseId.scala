package commons.repositories

trait BaseId[U] extends Any {
  def value: U
}
