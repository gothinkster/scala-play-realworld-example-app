package core.tags.test_helpers

import commons_test.test_helpers.WsScalaTestClientWithHost.TestWsClient
import commons_test.test_helpers.{ResponseTransformer, WsScalaTestClientWithHost}

import scala.concurrent.{ExecutionContext, Future}

class TagTestHelper(executionContext: ExecutionContext) extends WsScalaTestClientWithHost {

  private implicit val ec: ExecutionContext = executionContext

  def findAll[ReturnType](implicit testWsClient: TestWsClient,
                          responseTransformer: ResponseTransformer[ReturnType]): Future[ReturnType] = {
    wsUrl(s"/tags")
      .get()
      .map(responseTransformer(_))
  }
}