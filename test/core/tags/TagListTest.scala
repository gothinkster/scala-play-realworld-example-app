package core.tags

import articles.models.{ArticleWithTags, NewTag, TagListWrapper}
import articles.test_helpers.{Articles, Tags}
import commons_test.test_helpers.{RealWorldWithServerBaseTest, WithArticleTestHelper, WithUserTestHelper}
import play.api.libs.ws.WSResponse
import users.models.UserDetailsWithToken
import users.test_helpers.UserRegistrations

class TagListTest extends RealWorldWithServerBaseTest with WithArticleTestHelper with WithUserTestHelper {

  "Tags list" should "return empty array within wrapper when there are not any tags" in await {
    for {
      response <- tagTestHelper.findAll[WSResponse]
    } yield {
      response.status.mustBe(OK)
      response.json.as[TagListWrapper].tags.isEmpty.mustBe(true)
    }
  }

  private def createTag(newTag: NewTag) = {
    for {
      user <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      newArticle = Articles.hotToTrainYourDragon.copy(tagList = Seq(newTag.name))
      article <- articleTestHelper.create[ArticleWithTags](newArticle, user.token)
    } yield article
  }

  it should "return array with one tag within wrapper" in await {
    val newTag = Tags.dragons
    for {
      _ <- createTag(newTag)
      response <- tagTestHelper.findAll[WSResponse]
    } yield {
      response.status.mustBe(OK)
      val tags = response.json.as[TagListWrapper].tags
      tags.isEmpty.mustBe(false)
      tags.head.mustBe(newTag.name)
    }
  }

}
