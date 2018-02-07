package testhelpers

import commons.services.ActionRunner
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.routing._
import slick.dbio.DBIO

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, Future}

object TestUtils {

  val config = Map(
    "play.evolutions.enabled" -> "true",
    "play.evolutions.autoApply" -> "true",
    "slick.dbs.default.profile" -> "slick.jdbc.H2Profile$",
    "slick.dbs.default.db.driver" -> "org.h2.Driver",
    "slick.dbs.default.db.url" -> "jdbc:h2:mem:play;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
    "slick.dbs.default.db.user" -> "user",
    "slick.dbs.default.db.password" -> ""
  )

  def appWithEmbeddedDb: Application = new GuiceApplicationBuilder()
    .configure(config)
    .build

  def appWithEmbeddedDbWithFakeRoutes(router: Router): Application = new GuiceApplicationBuilder()
    .configure(config)
    .router(router)
    .build

  def runAndAwaitResult[T](action: DBIO[T])(implicit actionRunner: ActionRunner,
                                            duration: Duration = new DurationInt(1).minute): T = {
    val future: Future[T] = actionRunner.runTransactionally(action)
    Await.result(future, duration)
  }
}
