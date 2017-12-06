package core.articles

import core.articles.config._
import core.articles.models.{ArticlePage, ArticleTag, ArticleWithTags}
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class ArticleControllerGetArticlesTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "articles"

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def tagPopulator(implicit testComponents: AppWithTestComponents): TagPopulator = {
    testComponents.tagPopulator
  }

  def articleTagPopulator(implicit testComponents: AppWithTestComponents): ArticleTagPopulator = {
    testComponents.articleTagPopulator
  }

  "articles page" should {

    "return single article and article count" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon.copy(tags = Nil)
      val persistedArticle = articlePopulator.save(newArticle)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("limit" -> "5", "offset" -> "0")
        .get())

      // then
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
      page.articles.head.mustBe(ArticleWithTags(persistedArticle, Nil))
    }

    "return single article with dragons tag and article count" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedArticle = articlePopulator.save(newArticle)
      val persistedTag = tagPopulator.save(Tags.dragons)
      articleTagPopulator.save(ArticleTag.from(persistedArticle, persistedTag))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("limit" -> "5", "offset" -> "0")
        .get())

      // then
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
      page.articles.head.mustBe(ArticleWithTags.fromArticleAndRawTags(persistedArticle, Seq(Tags.dragons.name)))
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

    "return two articles sorted by last updated date desc by default" in {
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
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(2L)
      page.articles.head.id.mustBe(persistedNewerArticle.id)
      page.articles.tail.head.id.mustBe(persistedArticle.id)
    }
  }
}
