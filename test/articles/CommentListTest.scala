package articles

import articles.models.{ArticleWithTags, CommentList, CommentWithAuthor}
import articles.test_helpers.{Articles, Comments}
import commons_test.test_helpers.{RealWorldWithServerBaseTest, WithArticleTestHelper, WithUserTestHelper}
import play.api.libs.ws.WSResponse
import users.models.UserDetailsWithToken
import users.test_helpers.UserRegistrations

class CommentListTest extends RealWorldWithServerBaseTest with WithArticleTestHelper with WithUserTestHelper {

  "Comment list" should "return empty array if article does not have any comments" in await {
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      articleWithTags <- articleTestHelper.create[ArticleWithTags](Articles.hotToTrainYourDragon, userDetailsWithToken.token)

      response <- commentTestHelper.list[WSResponse](articleWithTags)
    } yield {
      response.status.mustBe(OK)
      val comments = response.json.as[CommentList].comments
      comments.isEmpty.mustBe(true)
    }
  }

  it should "return given article's comment " in await {
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      articleWithTags <- articleTestHelper.create[ArticleWithTags](Articles.hotToTrainYourDragon, userDetailsWithToken.token)
      comment <- commentTestHelper.create[CommentWithAuthor](articleWithTags, Comments.yummy, userDetailsWithToken.token)

      response <- commentTestHelper.list[WSResponse](articleWithTags)
    } yield {
      response.status.mustBe(OK)
      val comments = response.json.as[CommentList].comments
      comments.size.mustBe(1)
      comments.head.id.mustBe(comment.id)
    }
  }

  it should "return two comments, newer a the top" in await {
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      articleWithTags <- articleTestHelper.create[ArticleWithTags](Articles.hotToTrainYourDragon, userDetailsWithToken.token)
      _ <- commentTestHelper.create[CommentWithAuthor](articleWithTags, Comments.yummy, userDetailsWithToken.token)
      newerComment <- commentTestHelper.create[CommentWithAuthor](articleWithTags, Comments.yummy, userDetailsWithToken.token)

      response <- commentTestHelper.list[WSResponse](articleWithTags)
    } yield {
      response.status.mustBe(OK)
      val comments = response.json.as[CommentList].comments
      comments.size.mustBe(2)
      comments.head.id.mustBe(newerComment.id)
    }
  }

}
