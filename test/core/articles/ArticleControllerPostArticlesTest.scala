package core.articles

import java.time.LocalDateTime

import core.articles.config.{ArticlePopulator, Articles}
import core.articles.controllers.mappings.ArticleJsonMappings
import core.articles.models.ArticleWrapper
import commons.repositories.DateTimeProvider
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import testhelpers.{FixedDateTimeProvider, RealWorldWithServerBaseTest}
import core.users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}

class ArticleControllerPostArticlesTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "articles"

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  val dateTime: LocalDateTime = LocalDateTime.now

  "create article" should {

    "create valid article" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.username, registration.password)

      val newArticle = Articles.hotToTrainYourDragon

      val articleRequest: JsValue = JsObject(Map("article" -> Json.toJson(newArticle)))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${tokenResponse.token}")
        .post(articleRequest))

      // then
      response.status.mustBe(OK)
      val wrapper = response.json.as[ArticleWrapper]
      val article = wrapper.article
      article.title.mustBe(newArticle.title)
      article.modifiedAt.mustBe(dateTime)
    }

  }

  class RealWorldWithTestConfigWithFixedDateTimeProvider extends RealWorldWithTestConfig {
    override lazy val dateTimeProvider: DateTimeProvider = new FixedDateTimeProvider(dateTime)
  }

  override def components: RealWorldWithTestConfig = new RealWorldWithTestConfigWithFixedDateTimeProvider
}
