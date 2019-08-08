package articles

import articles.models.{ArticlePage, ArticleWithTags, MainFeedPageRequest}
import articles.test_helpers.{Articles, Tags}
import commons.models.Username
import commons_test.test_helpers.{RealWorldWithServerAndTestConfigBaseTest, WithArticleTestHelper, WithUserTestHelper}
import play.api.libs.ws.WSResponse
import users.models.UserDetailsWithToken
import users.test_helpers._

class ArticleListTest extends RealWorldWithServerAndTestConfigBaseTest with WithArticleTestHelper with WithUserTestHelper {

  it should "return single article with dragons tag and article count" in await {
    val newArticle = Articles.hotToTrainYourDragon.copy(tagList = Seq(Tags.dragons.name))
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      persistedArticle <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)

      response <- articleTestHelper.findAll[WSResponse](MainFeedPageRequest(limit = 5L, offset = 0L))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
      page.articles.head.id.mustBe(persistedArticle.id)
      page.articles.head.tagList.must(contain(Tags.dragons.name))
    }
  }

  it should "return empty array of articles and count when requested limit is 0" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      _ <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)

      response <- articleTestHelper.findAll[WSResponse](MainFeedPageRequest(limit = 0L, offset = 0L))
    } yield {
      response.status.mustBe(OK)
      response.json.as[ArticlePage].mustBe(ArticlePage(Nil, 1L))
    }
  }

  it should "return two articles sorted by last created date desc by default" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      persistedArticle <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)
      persistedNewerArticle <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)

      response <- articleTestHelper.findAll[WSResponse](MainFeedPageRequest(limit = 5L, offset = 0L))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(2L)
      page.articles.head.id.mustBe(persistedNewerArticle.id)
      page.articles.tail.head.id.mustBe(persistedArticle.id)
    }
  }

  it should "return article created by requested user" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      persistedArticle <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)

      response <- articleTestHelper.findAll[WSResponse](MainFeedPageRequest(limit = 5L, offset = 0L, author = Some(userDetailsWithToken.username)))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
      page.articles.head.id.mustBe(persistedArticle.id)
    }

  }

  it should "return empty array of articles when requested user have not created any articles" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      _ <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)

      response <- articleTestHelper.findAll[WSResponse](MainFeedPageRequest(limit = 5L, offset = 0L, author = Some(Username("not existing username"))))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(0L)
    }
  }

  it should "return article with requested tag" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      persistedArticle <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)

      response <- articleTestHelper.findAll[WSResponse](MainFeedPageRequest(limit = 5L, offset = 0L, tag = Some(newArticle.tagList.head)))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
      page.articles.head.id.mustBe(persistedArticle.id)
    }
  }

  it should "return empty array of articles when no articles with requested tag exist" in await {
    val newArticle = Articles.hotToTrainYourDragon.copy(tagList = Seq.empty)
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      _ <- articleTestHelper.create[ArticleWithTags](newArticle, userDetailsWithToken.token)

      response <- articleTestHelper.findAll[WSResponse](MainFeedPageRequest(limit = 5L, offset = 0L, tag = Some(Tags.dragons.name)))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(0L)
    }
  }

  it should "return article created by followed user" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      articleAuthor <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      authenticatedUser <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.kopernikRegistration)
      _ <- profileTestHelper.follow[WSResponse](articleAuthor.username, authenticatedUser.token)
      _ <- articleTestHelper.create[WSResponse](newArticle, articleAuthor.token)

      response <- articleTestHelper.findAll[WSResponse](MainFeedPageRequest(limit = 5L, offset = 0L, author = Some(articleAuthor.username)))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
    }
  }
}
