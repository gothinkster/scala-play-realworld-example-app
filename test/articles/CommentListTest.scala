package articles

import articles.config._
import articles.models.CommentList
import users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class CommentListTest extends RealWorldWithServerBaseTest {

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def commentPopulator(implicit testComponents: AppWithTestComponents): CommentPopulator = {
    testComponents.commentPopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  "Comment list" should "return empty array if article does not have any comments" in {
    // given
    val registration = UserRegistrations.petycjaRegistration
    val user = userRegistrationTestHelper.register(registration)

    val article = articlePopulator.save(Articles.hotToTrainYourDragon)(user)

    // when
    val response: WSResponse = await(wsUrl(s"/articles/${article.slug}/comments").get())

    // then
    response.status.mustBe(OK)
    val comments = response.json.as[CommentList].comments
    comments.isEmpty.mustBe(true)
  }

  it should "return given article's comment " in {
    // given
    val registration = UserRegistrations.petycjaRegistration
    val user = userRegistrationTestHelper.register(registration)

    val article = articlePopulator.save(Articles.hotToTrainYourDragon)(user)

    val comment = commentPopulator.save(Comments.yummy, article, user)

    // when
    val response: WSResponse = await(wsUrl(s"/articles/${article.slug}/comments").get())

    // then
    response.status.mustBe(OK)
    val comments = response.json.as[CommentList].comments
    comments.size.mustBe(1)
    comments.head.id.mustBe(comment.id)
  }

  it should "return two comments, newer a the top" in {
    // given
    val registration = UserRegistrations.petycjaRegistration
    val user = userRegistrationTestHelper.register(registration)

    val article = articlePopulator.save(Articles.hotToTrainYourDragon)(user)

    commentPopulator.save(Comments.yummy, article, user)
    val newerComment = commentPopulator.save(Comments.yummy, article, user)

    // when
    val response: WSResponse = await(wsUrl(s"/articles/${article.slug}/comments").get())

    // then
    response.status.mustBe(OK)
    val comments = response.json.as[CommentList].comments
    comments.size.mustBe(2)
    comments.head.id.mustBe(newerComment.id)
  }

}
