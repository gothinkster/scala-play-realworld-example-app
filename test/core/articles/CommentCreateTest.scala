package core.articles

import core.articles.config._
import core.articles.models.{CommentWrapper, NewCommentWrapper}
import core.users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class CommentCreateTest extends RealWorldWithServerBaseTest {

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  "Create comment" should "create comment for authenticated user" in {
    // given
    val registration = UserRegistrations.petycjaRegistration
    val user = userRegistrationTestHelper.register(registration)
    val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

    val article = articlePopulator.save(Articles.hotToTrainYourDragon)(user)

    val newComment = Comments.yummy
    val requestBody: JsValue = Json.toJson(NewCommentWrapper(newComment))

    // when
    val response: WSResponse = await(wsUrl(s"/articles/${article.slug}/comments")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
      .post(requestBody))

    // then
    response.status.mustBe(OK)
    val comment = response.json.as[CommentWrapper].comment
    comment.author.username.mustBe(user.username)
    comment.body.mustBe(newComment.body)
  }

}
