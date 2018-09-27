package articles.test_helpers

import articles.models._
import commons_test.test_helpers.WsScalaTestClientWithHost.TestWsClient
import commons_test.test_helpers.{ResponseTransformer, WsScalaTestClientWithHost}
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{EmptyBody, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class ArticleTestHelper(executionContext: ExecutionContext) extends WsScalaTestClientWithHost {

  def update(article: ArticleWithTags, articleUpdate: ArticleUpdate, token: String)
            (implicit testWsClient: TestWsClient): Future[WSResponse] = {

    wsUrl(s"/articles/${article.slug}")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token $token")
      .put(JsObject(Map("article" -> Json.toJson(articleUpdate))))
  }


  implicit private val ex: ExecutionContext = executionContext

  private def getQueryParams(mainFeedPageRequest: MainFeedPageRequest) = {
    import mainFeedPageRequest._
    val optionalParams = Seq(
      favorited.map("favorite" -> _.value),
      author.map("author" -> _.value),
      tag.map("tag" -> _)
    ).filter(_.isDefined)
      .map(_.get)

    Seq("limit" -> limit.toString, "offset" -> offset.toString) ++ optionalParams
  }

  def findAll[ReturnType](mainFeedPageRequest: MainFeedPageRequest, maybeToken: Option[String] = None)
                         (implicit testWsClient: TestWsClient,
                          responseTransformer: ResponseTransformer[ReturnType]): Future[ReturnType] = {
    val queryParams = getQueryParams(mainFeedPageRequest)
    val request = wsUrl( s"/articles").addQueryStringParameters(queryParams: _*)

    maybeToken
      .map(token => request.addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token $token"))
      .getOrElse(request)
      .get()
      .map(responseTransformer(_))
  }

  def favorite[ReturnType](slug: String, token: String)
                          (implicit testWsClient: TestWsClient,
                           responseTransformer: ResponseTransformer[ReturnType]): Future[ReturnType] = {
    wsUrl(s"/articles/$slug/favorite")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token $token")
      .post(EmptyBody)
      .map(responseTransformer(_))
  }


  def getBySlug[ReturnType](slug: String)(implicit testWsClient: TestWsClient,
                                          responseTransformer: ResponseTransformer[ReturnType]): Future[ReturnType] = {
    wsUrl(s"/articles/$slug")
      .get()
      .map(responseTransformer(_))
  }


  def delete(articleWithTags: ArticleWithTags, token: String)(implicit testWsClient: TestWsClient): Future[WSResponse] = {
    wsUrl(s"/articles/${articleWithTags.slug}")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token $token")
      .delete()
  }

  def create[ReturnType](newArticle: NewArticle, token: String)
                        (implicit testWsClient: TestWsClient,
                         responseTransformer: ResponseTransformer[ReturnType]): Future[ReturnType] = {
    val body = Json.toJson(NewArticleWrapper(newArticle))
    wsUrl("/articles")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token $token")
      .post(body)
      .map(responseTransformer(_))
  }

}
