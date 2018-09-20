package articles.test_helpers

import articles.models._
import commons_test.test_helpers.WsScalaTestClientWithHost.TestWsClient
import commons_test.test_helpers.{ResponseTransformer, WsScalaTestClientWithHost}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse

import scala.concurrent.{ExecutionContext, Future}

class CommentTestHelper(executionContext: ExecutionContext) extends WsScalaTestClientWithHost {

  implicit private val ec: ExecutionContext = executionContext

  def delete(comment: CommentWithAuthor, article: ArticleWithTags, token: String)
            (implicit testWsClient: TestWsClient): Future[WSResponse] = {
    wsUrl(s"/articles/${article.slug}/comments/${comment.id.value}")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token $token")
      .delete()
  }

  def list[ReturnType](article: ArticleWithTags)
                      (implicit testWsClient: TestWsClient,
                       responseTransformer: ResponseTransformer[ReturnType]): Future[ReturnType] = {
    wsUrl(s"/articles/${article.slug}/comments")
      .get()
      .map(responseTransformer(_))
  }

  def create[ReturnType](article: ArticleWithTags, newComment: NewComment, token: String)
                        (implicit testWsClient: TestWsClient,
                         responseTransformer: ResponseTransformer[ReturnType]): Future[ReturnType] = {
    wsUrl(s"/articles/${article.slug}/comments")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token $token")
      .post(Json.toJson(NewCommentWrapper(newComment)))
      .map(responseTransformer(_))
  }

}