package commons_test.test_helpers

import commons.services.ActionRunner
import play.api.test.DefaultAwaitTimeout
import slick.dbio.DBIO

import scala.concurrent.{Await, Future}

trait WithAwaitUtilities {
  _: DefaultAwaitTimeout =>

  def await[A](testBody: => Future[A]): A = {
    Await.result(testBody, defaultAwaitTimeout.duration)
  }

  def runAndAwait[A](testBody: => DBIO[A])(implicit actionRunner: ActionRunner): A = {
    await(actionRunner.run(testBody))
  }
}
