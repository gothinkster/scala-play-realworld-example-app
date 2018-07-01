package articles

import articles.config.{ArticlePopulator, Articles}
import articles.models.{ArticleUpdate, ArticleWrapper}
import users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class ArticleUpdateTest extends RealWorldWithServerBaseTest {

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  "Update article" should "update title and slug" in {
    // given
    val newArticle = Articles.hotToTrainYourDragon

    val registration = UserRegistrations.petycjaRegistration
    val user = userRegistrationTestHelper.register(registration)
    val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

    val persistedArticle = articlePopulator.save(newArticle)(user)

    val newTitle = "new title"
    val articleUpdate = ArticleUpdate(Some(newTitle))
    val requestBody: JsValue = JsObject(Map("article" -> Json.toJson(articleUpdate)))

    // when
    val response: WSResponse = await(wsUrl(s"/articles/${persistedArticle.slug}")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
      .put(requestBody))

    // then
    response.status.mustBe(OK)
    val article = response.json.as[ArticleWrapper].article
    article.title.mustBe(newTitle)
  }

}