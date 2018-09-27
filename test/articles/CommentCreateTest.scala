package articles

import articles.models._
import articles.test_helpers.{Articles, Comments}
import commons_test.test_helpers.{RealWorldWithServerBaseTest, WithArticleTestHelper, WithUserTestHelper}
import play.api.libs.ws.WSResponse
import users.models.UserDetailsWithToken
import users.test_helpers.UserRegistrations

class CommentCreateTest extends RealWorldWithServerBaseTest with WithArticleTestHelper with WithUserTestHelper {

  "Create comment" should "create comment for authenticated user" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      persistedArticle <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)
      newComment = Comments.yummy

      response <- commentTestHelper.create[WSResponse](persistedArticle, newComment, userDetailsWithToken.token)
    } yield {
      response.status.mustBe(OK)
      val comment = response.json.as[CommentWrapper].comment
      comment.author.username.mustBe(userDetailsWithToken.username)
      comment.body.mustBe(newComment.body)
    }
  }

}
