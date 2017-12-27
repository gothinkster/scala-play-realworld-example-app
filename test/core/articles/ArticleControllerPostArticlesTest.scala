package core.articles

import java.time.Instant

import commons.repositories.DateTimeProvider
import core.articles.config.{ArticlePopulator, Articles, TagPopulator, Tags}
import core.articles.models.ArticleWrapper
import core.users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import testhelpers.{FixedDateTimeProvider, RealWorldWithServerBaseTest}

class ArticleControllerPostArticlesTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "articles"

  def articlePopulator(implicit testComponents: AppWithTestComponents): ArticlePopulator = {
    testComponents.articlePopulator
  }

  def tagPopulator(implicit testComponents: AppWithTestComponents): TagPopulator = {
    testComponents.tagPopulator
  }

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  val dateTime: Instant = Instant.now

  "create article" should {

    "create valid article without tags" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      val newArticle = Articles.hotToTrainYourDragon.copy(tagList = Nil)

      val articleRequest: JsValue = JsObject(Map("article" -> Json.toJson(newArticle)))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .post(articleRequest))

      // then
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.title.mustBe(newArticle.title)
      article.updatedAt.mustBe(dateTime)
      article.tagList.isEmpty.mustBe(true)
    }

    "create valid article with dragons tag" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      val newArticle = Articles.hotToTrainYourDragon

      val articleRequest: JsValue = JsObject(Map("article" -> Json.toJson(newArticle)))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .post(articleRequest))

      // then
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.title.mustBe(newArticle.title)
      article.updatedAt.mustBe(dateTime)
      article.tagList.size.mustBe(1L)
    }

    "create article and associate it with existing dragons tag" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      tagPopulator.save(Tags.dragons)

      val newArticle = Articles.hotToTrainYourDragon
      val articleRequest: JsValue = JsObject(Map("article" -> Json.toJson(newArticle)))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .post(articleRequest))

      // then
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.tagList.size.mustBe(1L)
      tagPopulator.all.size.mustBe(1L)
    }

    "create article and set author" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      val newArticle = Articles.hotToTrainYourDragon
      val articleRequest: JsValue = JsObject(Map("article" -> Json.toJson(newArticle)))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .post(articleRequest))

      // then
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.author.username.mustBe(registration.username)
    }

    "generate slug based on title" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

      val titlePart1 = "the"
      val titlePart2 = "title"

      val newArticle = Articles.hotToTrainYourDragon.copy(title = s"$titlePart1 $titlePart2")
      val articleRequest: JsValue = JsObject(Map("article" -> Json.toJson(newArticle)))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
        .post(articleRequest))

      // then
      response.status.mustBe(OK)
      val slug = response.json.as[ArticleWrapper].article.slug
      slug.contains(include(titlePart1).and(include(titlePart2)))
    }

  }

  class RealWorldWithTestConfigWithFixedDateTimeProvider extends RealWorldWithTestConfig {
    override lazy val dateTimeProvider: DateTimeProvider = new FixedDateTimeProvider(dateTime)
  }

  override def components: RealWorldWithTestConfig = new RealWorldWithTestConfigWithFixedDateTimeProvider
}
