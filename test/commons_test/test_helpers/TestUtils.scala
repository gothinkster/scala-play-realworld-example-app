package commons_test.test_helpers

import commons.services.ActionRunner
import slick.dbio.DBIO

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

object TestUtils {

  val config: Map[String, String] = Map(
    "play.evolutions.enabled" -> "true",
    "play.evolutions.autoApply" -> "true",
    "slick.dbs.default.profile" -> "slick.jdbc.H2Profile$",
    "slick.dbs.default.db.driver" -> "org.h2.Driver",
    "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;DATABASE_TO_UPPER=false",
    "slick.dbs.default.db.user" -> "user",
    "slick.dbs.default.db.password" -> ""
  )

  def runAndAwaitResult[T](action: DBIO[T])(implicit actionRunner: ActionRunner,
                                            duration: Duration = new DurationInt(1).minute): T = {
    val future: Future[T] = actionRunner.runTransactionally(action)
    Await.result(future, duration)
  }
}
