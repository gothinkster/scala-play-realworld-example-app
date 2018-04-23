package core.tags

import core.articles.config.{TagPopulator, Tags}
import core.articles.models.TagListWrapper
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class TagListTest extends RealWorldWithServerBaseTest {

  def tagPopulator(implicit testComponents: AppWithTestComponents): TagPopulator = {
    testComponents.tagPopulator
  }

  "Tags list" should "return empty array within wrapper when there are not any tags" in {
    // when
    val response: WSResponse = await(wsUrl("/tags").get())

    // then
    response.status.mustBe(OK)
    response.json.as[TagListWrapper].tags.isEmpty.mustBe(true)
  }

  it should "return array with one tag within wrapper" in {
    // given
    val newTag = Tags.dragons
    tagPopulator.save(newTag)

    // when
    val response: WSResponse = await(wsUrl("/tags").get())

    // then
    response.status.mustBe(OK)
    response.json.as[TagListWrapper].tags.isEmpty.mustBe(false)
    response.json.as[TagListWrapper].tags.head.mustBe(newTag.name)
  }

}
