package testhelpers

import commons.services.ActionRunner
import slick.dbio.DBIO

import scala.concurrent.duration.{Duration, DurationInt}

trait Populator {

  implicit protected val awaitDuration: Duration = new DurationInt(1).minute

  protected def runAndAwait[T](action: DBIO[T])(implicit actionRunner: ActionRunner): T = {
    TestUtils.runAndAwaitResult(action)
  }
}