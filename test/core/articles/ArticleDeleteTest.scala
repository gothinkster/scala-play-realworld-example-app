package core.articles

import core.articles.config._
import core.articles.models.ArticleTag
import core.users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class ArticleDeleteTest extends RealWorldWithServerBaseTest {

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  def tagPopulator(implicit testComponents: AppWithTestComponents): TagPopulator = {
    testComponents.tagPopulator
  }

  def articleTagPopulator(implicit testComponents: AppWithTestComponents): ArticleTagPopulator = {
    testComponents.articleTagPopulator
  }

  "Delete article" should {

    "delete associated tag too" in {
      // given
      val newArticle = Articles.hotToTrainYourDragon

      val registration = UserRegistrations.petycjaRegistration
      val user = userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      val persistedArticle = articlePopulator.save(newArticle)(user)

      val persistedTag = tagPopulator.save(Tags.dragons)
      articleTagPopulator.save(ArticleTag.from(persistedArticle, persistedTag))

      // when
      val response: WSResponse = await(wsUrl(s"/articles/${persistedArticle.slug}")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .delete())

      // then
      response.status.mustBe(OK)
    }

  }

}