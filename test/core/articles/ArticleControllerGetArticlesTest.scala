package core.articles

import core.articles.config._
import core.articles.models.{ArticlePage, ArticleTag, ArticleWithTags}
import core.users.test_helpers.{UserPopulator, Users}
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class ArticleControllerGetArticlesTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "articles"

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def userPopulator(implicit testComponents: AppWithTestComponents): UserPopulator = {
    testComponents.userPopulator
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
      val newArticle = Articles.hotToTrainYourDragon.copy(tagList = Nil)
      val persistedUser = userPopulator.save(Users.petycja)
      val persistedArticle = articlePopulator.save(newArticle)(persistedUser)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("limit" -> "5", "offset" -> "0")
        .get())

      // then
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
      page.articles.head.mustBe(ArticleWithTags(persistedArticle, Nil, persistedUser))
    }

    "return single article with dragons tag and article count" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedUser = userPopulator.save(Users.petycja)
      val persistedArticle = articlePopulator.save(newArticle)(persistedUser)
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
      page.articles.head.mustBe(
        ArticleWithTags.fromTagValues(persistedArticle, Seq(Tags.dragons.name), persistedUser))
    }

    "return empty array of articles and count when requested limit is 0" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedUser = userPopulator.save(Users.petycja)
      articlePopulator.save(newArticle)(persistedUser)

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
      val persistedUser = userPopulator.save(Users.petycja)
      val persistedArticle = articlePopulator.save(newArticle)(persistedUser)

      val newerArticle = Articles.hotToTrainYourDragon
      val persistedNewerArticle = articlePopulator.save(newerArticle)(persistedUser)

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

    "return article created by requested user" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedUser = userPopulator.save(Users.petycja)
      val persistedArticle = articlePopulator.save(newArticle)(persistedUser)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("author" -> persistedUser.username.value)
        .get())

      // then
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
      page.articles.head.id.mustBe(persistedArticle.id)
    }

    "return empty array of articles when requested user have not created any articles" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedUser = userPopulator.save(Users.petycja)
      articlePopulator.save(newArticle)(persistedUser)

      val anotherUser = userPopulator.save(Users.kopernik)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("author" -> anotherUser.username.value)
        .get())

      // then
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(0L)
    }

    "return article with requested tag" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedUser = userPopulator.save(Users.petycja)
      val persistedArticle = articlePopulator.save(newArticle)(persistedUser)

      val persistedTag = tagPopulator.save(Tags.dragons)
      articleTagPopulator.save(ArticleTag.from(persistedArticle, persistedTag))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("tag" -> persistedTag.name)
        .get())

      // then
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
      page.articles.head.id.mustBe(persistedArticle.id)
    }

    "return empty array of articles when no articles with requested tag exist" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon
      val persistedUser = userPopulator.save(Users.petycja)
      articlePopulator.save(newArticle)(persistedUser)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addQueryStringParameters("tag" -> Tags.dragons.name)
        .get())

      // then
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(0L)
    }

  }
}
