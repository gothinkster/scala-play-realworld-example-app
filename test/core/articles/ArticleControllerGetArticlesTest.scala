package core.articles

import core.articles.config.{ArticlePopulator, Articles}
import core.articles.controllers.mappings.ArticleJsonMappings
import core.articles.models.ArticlePage
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class ArticleControllerGetArticlesTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "articles"

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  "articles page" should {

    "return single article and article count" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedArticle = articlePopulator.save(newArticle)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("limit" -> "5", "offset" -> "0")
        .get())

      // then
      response.status.mustBe(OK)
      response.json.as[ArticlePage].mustBe(ArticlePage(List(persistedArticle), 1L))
    }

    "return empty array of articles and count when requested limit is 0" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      articlePopulator.save(newArticle)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("limit" -> "0", "offset" -> "0")
        .get())

      // then
      response.status.mustBe(OK)
      response.json.as[ArticlePage].mustBe(ArticlePage(Nil, 1L))
    }

    "return two articles sorted by last modified date desc by default" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedArticle = articlePopulator.save(newArticle)

      val newerArticle = Articles.hotToTrainYourDragon
      val persistedNewerArticle = articlePopulator.save(newerArticle)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("limit" -> "5", "offset" -> "0")
        .get())

      // then
      response.status.mustBe(OK)
      response.json.as[ArticlePage].mustBe(ArticlePage(List(persistedNewerArticle, persistedArticle), 2L))
    }
  }
}
