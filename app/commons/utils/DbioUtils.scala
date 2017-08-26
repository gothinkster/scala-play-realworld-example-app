package commons.utils

import slick.dbio.DBIO

object DbioUtils {
  def optionToDbio[A](someVal: Option[A], failureReason: Throwable = new NoSuchElementException): DBIO[A] = {
    someVal match {
      case Some(v) => DBIO.successful(v)
      case None => DBIO.failed(failureReason)
    }
  }
}
