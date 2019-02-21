package commons_test.test_helpers

import commons_test.test_helpers.WsScalaTestClientWithHost.TestWsClient
import config.RealWorldComponents
import org.scalatest._
import org.scalatestplus.play.components.OneServerPerTestWithComponents
import play.api.Configuration
import play.api.http.Status
import play.api.test.DefaultAwaitTimeout

import scala.concurrent.ExecutionContext

trait RealWorldWithServerBaseTest extends FlatSpec
  with MustMatchers
  with OptionValues
  with WsScalaTestClientWithHost
  with OneServerPerTestWithComponents
  with Status
  with DefaultAwaitTimeout
  with WithAwaitUtilities
  with BeforeAndAfterEach
  with WithTestExecutionContext {

  override implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  implicit val host: Host = Host("http://localhost:")

  implicit var testComponents: RealWorldWithTestConfig = _

  override def components: RealWorldWithTestConfig = {
    testComponents = createComponents
    testComponents
  }

  implicit def wsClientWithConnectionData: TestWsClient = {
    TestWsClient(host, portNumber, testComponents.wsClient)
  }

  def createComponents: RealWorldWithTestConfig = {
    new RealWorldWithTestConfig
  }

  class RealWorldWithTestConfig extends RealWorldComponents(context) {

    override def configuration: Configuration = {
      val testConfig = Configuration.from(TestUtils.config)
      val config = super.configuration
      config ++ testConfig
    }

  }
}
