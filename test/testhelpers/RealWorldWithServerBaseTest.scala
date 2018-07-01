package testhelpers

import articles.config.ArticleTestComponents
import config.RealWorldComponents
import users.config.UserTestComponents
import org.scalatest._
import org.scalatestplus.play.{PlaySpec, WsScalaTestClient}
import org.scalatestplus.play.components.OneServerPerTestWithComponents
import play.api.Configuration
import play.api.db.evolutions.Evolutions
import play.api.http.Status
import play.api.libs.ws.WSClient
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

trait RealWorldWithServerBaseTest extends FlatSpec
  with MustMatchers
  with OptionValues
  with WsScalaTestClient
  with OneServerPerTestWithComponents
  with Status
  with DefaultAwaitTimeout
  with FutureAwaits
  with BeforeAndAfterEach {

  implicit val defaultAwaitDuration: Duration = defaultAwaitTimeout.duration

  class RealWorldWithTestConfig extends RealWorldComponents(context) {

    override def configuration: Configuration = {
      val testConfig = Configuration.from(TestUtils.config)
      val config = super.configuration
      config ++ testConfig
    }

    applicationLifecycle.addStopHook(() => {
      Future(cleanUpInMemDb)
    })

    private def cleanUpInMemDb: Unit = {
      Evolutions.cleanupEvolutions(dbApi.database("default"))
    }

  }

  class AppWithTestComponents extends RealWorldWithTestConfig
    with ArticleTestComponents
    with UserTestComponents

   override def components: AppWithTestComponents = {
    testComponents = createComponents
    testComponents
  }

  def createComponents: AppWithTestComponents = {
    new AppWithTestComponents
  }

  implicit def wsClient(implicit testComponents: AppWithTestComponents): WSClient = testComponents.wsClient

  implicit var testComponents: AppWithTestComponents = _

}
