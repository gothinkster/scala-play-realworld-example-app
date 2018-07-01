package articles

import articles.config.{ArticlePopulator, Articles}
import articles.models.ArticleWrapper
import users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}
import play.api.http.HeaderNames
import play.api.libs.ws.{EmptyBody, WSResponse}
import testhelpers.RealWorldWithServerBaseTest

class ArticleFavoriteTest extends RealWorldWithServerBaseTest {

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  "Favorite article" should "mark article as favorited for current user" in {
    // given
    val newArticle = Articles.hotToTrainYourDragon

    val registration = UserRegistrations.petycjaRegistration
    val user = userRegistrationTestHelper.register(registration)
    val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

    val persistedArticle = articlePopulator.save(newArticle)(user)

    // when
    val response: WSResponse = await(wsUrl(s"/articles/${persistedArticle.slug}/favorite")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
      .post(EmptyBody))

    // then
    response.status.mustBe(OK)
    val article = response.json.as[ArticleWrapper].article
    article.title.mustBe(newArticle.title)
    article.favorited.mustBe(true)
    article.favoritesCount.mustBe(1)
  }

}
