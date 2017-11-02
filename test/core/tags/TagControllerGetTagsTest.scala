package core.tags

import core.articles.config.{TagPopulator, Tags}
import core.articles.models.TagListWrapper
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class TagControllerGetTagsTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "tags"

  def tagPopulator(implicit testComponents: AppWithTestComponents): TagPopulator = {
    testComponents.tagPopulator
  }

  "tags list" should {

    "return empty array in a wrapper when there is not any tag" in {
      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath").get())

      // then
      response.status.mustBe(OK)
      response.json.as[TagListWrapper].tags.isEmpty.mustBe(true)
    }

    "return array with one tag in a wrapper" in {
      // given
      val newTag = Tags.dragons
      tagPopulator.save(newTag)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath").get())

      // then
      response.status.mustBe(OK)
      response.json.as[TagListWrapper].tags.isEmpty.mustBe(false)
      response.json.as[TagListWrapper].tags.head.mustBe(newTag.name)
    }

  }
}
