package commons_test.test_helpers

import commons_test.test_helpers.WsScalaTestClientWithHost.TestWsClient
import org.scalatestplus.play.PortNumber
import play.api.libs.ws.{WSClient, WSRequest}

trait WsScalaTestClientWithHost {

  def wsUrl(url: String)(implicit testWsClient: TestWsClient): WSRequest = {
    testWsClient.url(url)
  }

}

object WsScalaTestClientWithHost {
  case class TestWsClient(host: Host, portNumber: PortNumber, wsClient: WSClient) {
    def url(url: String): WSRequest = {
      wsClient.url(host.value + portNumber.value + url)
    }
  }
}