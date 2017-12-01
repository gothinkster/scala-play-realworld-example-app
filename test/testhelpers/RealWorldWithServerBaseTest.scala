package testhelpers

import core.articles.config.ArticleTestComponents
import core.config.RealWorldComponents
import core.users.config.UserTestComponents
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.components.OneServerPerTestWithComponents
import play.api.Configuration
import play.api.db.evolutions.Evolutions
import play.api.http.Status
import play.api.libs.ws.WSClient
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import slick.dbio.DBIO

import scala.concurrent.duration.Duration

trait RealWorldWithServerBaseTest extends PlaySpec
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

  }

  class AppWithTestComponents extends RealWorldWithTestConfig
    with ArticleTestComponents
    with UserTestComponents

  override def components: RealWorldWithTestConfig = {
    new RealWorldWithTestConfig
  }

  implicit def wsClient(implicit testComponents: AppWithTestComponents): WSClient = testComponents.wsClient

  implicit var testComponents: AppWithTestComponents = _

  private def cleanUpInMemDb(c: RealWorldWithTestConfig) = {
    Evolutions.cleanupEvolutions(c.dbApi.database("default"))
  }

  override protected def beforeEach(): Unit = {
    testComponents = new AppWithTestComponents
  }

  override protected def afterEach(): Unit = {
    cleanUpInMemDb(new AppWithTestComponents)
  }

  def runAndAwaitResult[T](action: DBIO[T])(implicit components: RealWorldComponents): T = {
    TestUtils.runAndAwaitResult(action)(components.actionRunner, defaultAwaitDuration)
  }

}
