package articles

import articles.models.{ArticleWithTags, CommentList, CommentWithAuthor}
import articles.test_helpers.{Articles, Comments}
import commons_test.test_helpers.{RealWorldWithServerAndTestConfigBaseTest, WithArticleTestHelper, WithUserTestHelper}
import users.models.UserDetailsWithToken
import users.test_helpers.UserRegistrations

class CommentDeleteTest extends RealWorldWithServerAndTestConfigBaseTest with WithArticleTestHelper with WithUserTestHelper {

  "Delete comment" should "allow to delete authenticated user's comment" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      article <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)
      comment <- commentTestHelper.create[CommentWithAuthor](article, Comments.yummy, userDetailsWithToken.token)

      response <- commentTestHelper.delete(comment, article, userDetailsWithToken.token)
      commentList <- commentTestHelper.list[CommentList](article)
    } yield {
      response.status.mustBe(OK)
      commentList.comments.isEmpty.mustBe(true)
    }
  }

  it should "not allow to delete someone else's comment" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      author <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      article <- articleTestHelper.create[ArticleWithTags](newArticle, author.token)
      comment <- commentTestHelper.create[CommentWithAuthor](article, Comments.yummy, author.token)
      reader <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.kopernikRegistration)

      response <- commentTestHelper.delete(comment, article, reader.token)
      commentList <- commentTestHelper.list[CommentList](article)
    } yield {
      response.status.mustBe(FORBIDDEN)
      commentList.comments.isEmpty.mustBe(false)
    }
  }

}
