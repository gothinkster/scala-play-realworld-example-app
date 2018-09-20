package articles

import articles.models.{ArticleUpdate, ArticleWithTags, ArticleWrapper}
import articles.test_helpers.Articles
import commons_test.test_helpers.{RealWorldWithServerBaseTest, WithArticleTestHelper, WithUserTestHelper}
import users.models.UserDetailsWithToken
import users.test_helpers.UserRegistrations

class ArticleUpdateTest extends RealWorldWithServerBaseTest with WithArticleTestHelper with WithUserTestHelper {

  "Update article" should "update title and slug" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      persistedArticle <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)

      articleUpdate = ArticleUpdate(Some("new title"))

      response <- articleTestHelper.update(persistedArticle, articleUpdate, userDetailsWithToken.token)
    } yield {
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.title.mustBe(articleUpdate.title.get)
    }
  }

}