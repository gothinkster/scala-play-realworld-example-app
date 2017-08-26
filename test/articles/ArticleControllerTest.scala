package articles

import articles.config.{ArticlePopulator, Articles}
import articles.controllers.ArticlePage
import articles.controllers.ArticlePageJsonMappings._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSClient, WSResponse}
import testhelpers.RealWorldWithServerBaseTest
import articles.controllers.mappings.ArticleJsonMappings._

class ArticleControllerTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "articles"

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  implicit def wsClient(implicit testComponents: AppWithTestComponents): WSClient = testComponents.wsClient

  "GET articles" should {

    "return single article and article count" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedArticle = articlePopulator.save(newArticle)

      val expectedBody: JsValue = Json.toJson(ArticlePage(List(persistedArticle), 1L))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("limit" -> "5", "offset" -> "0")
        .get())

      // then
      response.status.mustBe(OK)
      response.body.mustBe(expectedBody.toString())
    }

    "return empty array of articles and count when requested limit is 0" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      articlePopulator.save(newArticle)

      val expectedBody: JsValue = Json.toJson(ArticlePage(Nil, 1L))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("limit" -> "0", "offset" -> "0")
        .get())

      // then
      response.status.mustBe(OK)
      response.body.mustBe(expectedBody.toString())
    }

    "return two articles sorted by last modified date desc by default" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedArticle = articlePopulator.save(newArticle)

      val newerArticle = Articles.hotToTrainYourDragon
      val persistedNewerArticle = articlePopulator.save(newerArticle)

      val expectedBody: JsValue = Json.toJson(ArticlePage(List(persistedNewerArticle, persistedArticle), 2L))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("limit" -> "5", "offset" -> "0")
        .get())

      // then
      response.status.mustBe(OK)
      response.body.mustBe(expectedBody.toString())
    }
  }
}
