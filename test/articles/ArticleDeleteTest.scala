package articles

import articles.models.ArticleWithTags
import articles.test_helpers.Articles
import commons_test.test_helpers.{RealWorldWithServerAndTestConfigBaseTest, WithArticleTestHelper, WithUserTestHelper}
import core.tags.test_helpers.TagTestHelper
import play.api.libs.ws.WSResponse
import users.models.UserDetailsWithToken
import users.test_helpers.UserRegistrations

class ArticleDeleteTest extends RealWorldWithServerAndTestConfigBaseTest with WithArticleTestHelper with WithUserTestHelper {

  def tagsTestHelper: TagTestHelper = new TagTestHelper(executionContext)

  "Delete article" should "delete article" in await {
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      token = userDetailsWithToken.token
      articleWithTags <- articleTestHelper.create[ArticleWithTags](Articles.hotToTrainYourDragon, token)
      response <- articleTestHelper.delete(articleWithTags, token)
      getBySlugResponse: WSResponse <- articleTestHelper.getBySlug[WSResponse](articleWithTags.slug)
    } yield {
      response.status.mustBe(OK)
      getBySlugResponse.status.mustBe(NOT_FOUND)
    }
  }

}