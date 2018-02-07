package core.articles

import core.articles.config._
import core.users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}
import play.api.http.HeaderNames
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class CommentDeleteTest extends RealWorldWithServerBaseTest {

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def commentPopulator(implicit testComponents: AppWithTestComponents): CommentPopulator = {
    testComponents.commentPopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  "Delete comment" should {

    "allow to delete authenticated user's comment" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      val user = userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      val article = articlePopulator.save(Articles.hotToTrainYourDragon)(user)

      val comment = commentPopulator.save(Comments.yummy, article, user)

      // when
      val response: WSResponse = await(wsUrl(s"/articles/${article.slug}/comments/${comment.id.value}")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .delete())

      // then
      response.status.mustBe(OK)
      commentPopulator.findById(comment.id).isEmpty.mustBe(true)
    }

    "not allow to delete someone else's comment" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      val author = userRegistrationTestHelper.register(registration)

      val article = articlePopulator.save(Articles.hotToTrainYourDragon)(author)

      val comment = commentPopulator.save(Comments.yummy, article, author)

      val kopernikRegistration = UserRegistrations.kopernikRegistration
      userRegistrationTestHelper.register(kopernikRegistration)
      val tokenResponse = userRegistrationTestHelper.getToken(kopernikRegistration.email, kopernikRegistration.password)

      // when
      val response: WSResponse = await(wsUrl(s"/articles/${article.slug}/comments/${comment.id.value}")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .delete())

      // then
      response.status.mustBe(FORBIDDEN)
      commentPopulator.findById(comment.id).isDefined.mustBe(true)
    }

  }

}
