package commons.models

sealed trait Direction

object Ascending extends Direction

object Descending extends Direction

case class Ordering(property: Property[_], direction: Direction = Descending) {
  def toAsc: Ordering = copy(direction = Ascending)

  def toDesc: Ordering = copy(direction = Descending)
}