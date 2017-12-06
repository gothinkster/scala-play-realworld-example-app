package core.articles

import java.time.Instant

import commons.repositories.DateTimeProvider
import core.articles.config.{ArticlePopulator, Articles}
import core.articles.models.ArticleWrapper
import core.users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import testhelpers.{FixedDateTimeProvider, RealWorldWithServerBaseTest}

class ArticleControllerPostArticlesTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "articles"

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  val dateTime: Instant = Instant.now

  "create article" should {

    "create valid article without tags" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      val newArticle = Articles.hotToTrainYourDragon.copy(tags = Nil)

      val articleRequest: JsValue = JsObject(Map("article" -> Json.toJson(newArticle)))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .post(articleRequest))

      // then
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.title.mustBe(newArticle.title)
      article.updatedAt.mustBe(dateTime)
      article.tags.isEmpty.mustBe(true)
    }

    "create valid article with dragons tag" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      val newArticle = Articles.hotToTrainYourDragon

      val articleRequest: JsValue = JsObject(Map("article" -> Json.toJson(newArticle)))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .post(articleRequest))

      // then
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.title.mustBe(newArticle.title)
      article.updatedAt.mustBe(dateTime)
      article.tags.size.mustBe(1L)
    }

  }

  class RealWorldWithTestConfigWithFixedDateTimeProvider extends RealWorldWithTestConfig {
    override lazy val dateTimeProvider: DateTimeProvider = new FixedDateTimeProvider(dateTime)
  }

  override def components: RealWorldWithTestConfig = new RealWorldWithTestConfigWithFixedDateTimeProvider
}
