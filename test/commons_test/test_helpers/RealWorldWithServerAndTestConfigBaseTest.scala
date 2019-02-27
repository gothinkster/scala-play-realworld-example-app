package commons_test.test_helpers

import java.util.concurrent.Executors

import commons_test.test_helpers.RealWorldWithServerAndTestConfigBaseTest.RealWorldWithTestConfig
import commons_test.test_helpers.WsScalaTestClientWithHost.TestWsClient
import config.RealWorldComponents
import org.scalatest._
import play.api.ApplicationLoader.Context
import play.api.Configuration
import play.api.http.Status
import play.api.test.DefaultAwaitTimeout

import scala.concurrent.ExecutionContext

trait RealWorldWithServerBaseTest extends FlatSpec
  with MustMatchers
  with OptionValues
  with WsScalaTestClientWithHost
  with OneServerPerTestWithComponents_FixedForCompileTimeTestSetUp
  with Status
  with DefaultAwaitTimeout
  with WithAwaitUtilities
  with WithTestExecutionContext {

  override implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

  implicit val host: Host = Host("http://localhost:")

  override type TestComponents <: RealWorldWithTestConfig

  implicit def wsClientWithConnectionData: TestWsClient = {
    TestWsClient(host, portNumber, components.wsClient)
  }

}

object RealWorldWithServerAndTestConfigBaseTest {

  class RealWorldWithTestConfig(context: Context) extends RealWorldComponents(context) {

    override def configuration: Configuration = {
      val testConfig = Configuration.from(TestUtils.config)
      val config = super.configuration
      config ++ testConfig
    }

  }

}

class RealWorldWithServerAndTestConfigBaseTest extends RealWorldWithServerBaseTest {
  override type TestComponents = RealWorldWithTestConfig

  override def createComponents: TestComponents = {
      new RealWorldWithTestConfig(context)
    }
}