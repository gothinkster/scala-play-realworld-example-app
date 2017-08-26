package testhelpers

import articles.config.ArticleTestComponents
import config.ExampleComponents
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.components.OneServerPerTestWithComponents
import play.api.Configuration
import play.api.db.evolutions.Evolutions
import play.api.http.Status
import play.api.libs.ws.WSClient
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import slick.dbio.DBIO

trait RealWorldWithServerBaseTest extends PlaySpec
  with OneServerPerTestWithComponents
  with DefaultFutureDuration
  with DefaultExecutionContext
  with Status
  with DefaultAwaitTimeout
  with FutureAwaits
  with BeforeAndAfterEach {

  class RealWorldWithTestConfig extends ExampleComponents(context) {

    override def configuration: Configuration = {
      val testConfig = Configuration.from(TestUtils.config)
      val config = super.configuration
      val testConfigMerged = config ++ testConfig
      config.toString
      testConfigMerged
    }

    implicit lazy val testWsClient: WSClient = wsClient
  }

  class AppWithTestComponents extends RealWorldWithTestConfig with ArticleTestComponents

  override def components: RealWorldWithTestConfig = {
    new RealWorldWithTestConfig
  }

  implicit var testComponents: AppWithTestComponents = _

  private def cleanUpInMemDb(c: RealWorldWithTestConfig) = {
      Evolutions.cleanupEvolutions(c.dbApi.database("default"))
  }

  override protected  def beforeEach(): Unit = {
    testComponents = new AppWithTestComponents
  }

  override protected def afterEach(): Unit = {
    cleanUpInMemDb(new AppWithTestComponents)
  }

  def runAndAwaitResult[T](action: DBIO[T])(implicit components: ExampleComponents): T = {
    TestUtils.runAndAwaitResult(action)(components.actionRunner, duration)
  }

}
