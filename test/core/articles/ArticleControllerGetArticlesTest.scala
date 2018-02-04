package core.articles

import core.articles.config._
import core.articles.models.{ArticlePage, ArticleTag}
import core.users.config.FollowAssociationTestHelper
import core.users.models.{FollowAssociation, FollowAssociationId}
import core.users.test_helpers.{UserPopulator, UserRegistrationTestHelper, UserRegistrations, Users}
import play.api.http.HeaderNames
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

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  def followAssociationTestHelper(implicit testComponents: AppWithTestComponents): FollowAssociationTestHelper =
    testComponents.followAssociationTestHelper

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
      page.articles.head.id.mustBe(persistedArticle.id)
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
      page.articles.head.id.mustBe(persistedArticle.id)
      page.articles.head.tagList.must(contain(Tags.dragons.name))
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

    "return article created by followed user" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon

      val registration = UserRegistrations.petycjaRegistration
      val user = userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      articlePopulator.save(newArticle)(user)

      followAssociationTestHelper.save(FollowAssociation(FollowAssociationId(-1), user.id, user.id))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath/feed")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .get())

      // then
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
    }

  }
}
