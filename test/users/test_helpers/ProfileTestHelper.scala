package users.test_helpers

import commons.models.Username
import commons_test.test_helpers.{ResponseTransformer, WsScalaTestClientWithHost}
import commons_test.test_helpers.WsScalaTestClientWithHost.TestWsClient
import play.api.http.HeaderNames
import play.api.libs.ws.EmptyBody

import scala.concurrent.{ExecutionContext, Future}

class ProfileTestHelper(executionContext: ExecutionContext) extends WsScalaTestClientWithHost {

  def follow[ReturnType](username: Username, token: String)
                        (implicit testWsClient: TestWsClient,
                         responseTransformer: ResponseTransformer[ReturnType]): Future[ReturnType] = {
    wsUrl(s"/profiles/${username.value}/follow")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token $token")
      .post(EmptyBody)
      .map(responseTransformer(_))(executionContext)
  }

}