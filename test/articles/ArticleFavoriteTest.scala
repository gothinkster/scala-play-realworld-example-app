package articles

import articles.models.{ArticleWithTags, ArticleWrapper}
import articles.test_helpers.Articles
import commons_test.test_helpers.{RealWorldWithServerAndTestConfigBaseTest, WithArticleTestHelper, WithUserTestHelper}
import play.api.libs.ws.WSResponse
import users.models.UserDetailsWithToken
import users.test_helpers.{UserRegistrations, UserTestHelper}

class ArticleFavoriteTest extends RealWorldWithServerAndTestConfigBaseTest with WithArticleTestHelper with WithUserTestHelper {

  "Favorite article" should "mark article as favorited for current user" in await {
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      newArticle = Articles.hotToTrainYourDragon
      articleWithTags <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)
      response <- articleTestHelper.favorite[WSResponse](articleWithTags.slug, userDetailsWithToken.token)
    } yield {
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.title.mustBe(newArticle.title)
      article.favorited.mustBe(true)
      article.favoritesCount.mustBe(1)
    }
  }

}
