package articles

import java.time.Instant

import articles.models.ArticleWrapper
import articles.test_helpers.Articles
import commons.repositories.DateTimeProvider
import commons_test.test_helpers.{FixedDateTimeProvider, RealWorldWithServerBaseTest, WithArticleTestHelper, WithUserTestHelper}
import play.api.libs.ws.WSResponse
import users.models.UserDetailsWithToken
import users.test_helpers.UserRegistrations

class ArticleCreateTest extends RealWorldWithServerBaseTest with WithArticleTestHelper with WithUserTestHelper {

  val dateTime: Instant = Instant.now

  "Create article" should "create valid article without tags" in await {
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      newArticle = Articles.hotToTrainYourDragon.copy(tagList = Nil)
      response <- articleTestHelper.create[WSResponse](newArticle, userDetailsWithToken.token)
    } yield {
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.title.mustBe(newArticle.title)
      article.updatedAt.mustBe(dateTime)
      article.tagList.isEmpty.mustBe(true)
    }
  }

  it should "create valid article with dragons tag" in await {
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      newArticle = Articles.hotToTrainYourDragon
      response <- articleTestHelper.create[WSResponse](newArticle, userDetailsWithToken.token)
    } yield {
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.title.mustBe(newArticle.title)
      article.updatedAt.mustBe(dateTime)
      article.tagList.size.mustBe(1L)
    }
  }


  it should "create article and associate it with existing dragons tag" in await {
    def createFirstArticleToCreateDragonsTag(userDetailsWithToken: UserDetailsWithToken) = {
      articleTestHelper.create[WSResponse](Articles.hotToTrainYourDragon, userDetailsWithToken.token)
    }

    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      _ <- createFirstArticleToCreateDragonsTag(userDetailsWithToken)
      response <- articleTestHelper.create[WSResponse](Articles.hotToTrainYourDragon, userDetailsWithToken.token)
    } yield {
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.tagList.size.mustBe(1L)
    }
  }

  it should "create article and set author" in await {
    val registration = UserRegistrations.petycjaRegistration
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](registration)
      response <- articleTestHelper.create[WSResponse](Articles.hotToTrainYourDragon, userDetailsWithToken.token)
    } yield {
      response.status.mustBe(OK)
      val article = response.json.as[ArticleWrapper].article
      article.author.username.mustBe(registration.username)
    }
  }

  it should "generate slug based on title" in await {
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      titlePart1 = "the"
      titlePart2 = "title"
      response <- articleTestHelper.create[WSResponse](Articles.hotToTrainYourDragon, userDetailsWithToken.token)
    } yield {
      response.status.mustBe(OK)
      val slug = response.json.as[ArticleWrapper].article.slug
      slug.contains(include(titlePart1).and(include(titlePart2)))
    }
  }

  override def createComponents: RealWorldWithTestConfig = {
    new RealWorldWithTestConfigWithFixedDateTimeProvider
  }

  class RealWorldWithTestConfigWithFixedDateTimeProvider extends RealWorldWithTestConfig {
    override lazy val dateTimeProvider: DateTimeProvider = new FixedDateTimeProvider(dateTime)
  }

}

