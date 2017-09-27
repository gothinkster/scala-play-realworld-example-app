package articles

import java.time.LocalDateTime

import articles.config.{ArticlePopulator, Articles}
import articles.controllers.mappings.ArticleJsonMappings
import articles.models.ArticleWrapper
import commons.repositories.DateTimeProvider
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import testhelpers.{FixedDateTimeProvider, RealWorldWithServerBaseTest}
import users.config.{UserRegistrationTestHelper, UserRegistrations}

class ArticleControllerPostArticlesTest extends RealWorldWithServerBaseTest with ArticleJsonMappings {
  val apiPath: String = "articles"

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  val dateTime: LocalDateTime = LocalDateTime.now

  "POST articles" should {

    "create article" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)
      val token = userRegistrationTestHelper.authenticate(registration.login, registration.password)

      val newArticle = Articles.hotToTrainYourDragon

      val articleRequest: JsValue = JsObject(Map("article" -> Json.toJson(newArticle)))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer $token")
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
