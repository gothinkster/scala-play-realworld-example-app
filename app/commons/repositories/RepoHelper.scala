package commons.repositories

import commons.models.{Ascending, Descending, Direction}
import slick.ast.{Ordering => SlickOrdering}
import slick.lifted.{ColumnOrdered, Rep}

object RepoHelper {
  def createSlickColumnOrdered(column: Rep[_])(implicit direction: Direction): ColumnOrdered[_] = {
    val slickDirection = toSlickDirection(direction)
    ColumnOrdered(column, SlickOrdering(slickDirection))
  }

  private def toSlickDirection(direction: Direction): SlickOrdering.Direction = direction match {
    case Ascending => SlickOrdering.Asc
    case Descending => SlickOrdering.Desc
  }
}