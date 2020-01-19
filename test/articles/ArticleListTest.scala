package articles

import articles.models._
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

      response <- articleTestHelper.findAll[WSResponse](ArticlesAll(limit = 5L, offset = 0L))
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

      response <- articleTestHelper.findAll[WSResponse](ArticlesAll(limit = 0L, offset = 0L))
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

      response <- articleTestHelper.findAll[WSResponse](ArticlesAll(limit = 5L, offset = 0L))
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
      author <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      article <- articleTestHelper.create[ArticleWithTags](newArticle, author.token)
      response <- articleTestHelper.findAll[WSResponse](ArticlesByAuthor(limit = 5L, offset = 0L, author = author.username))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
      page.articles.head.id.mustBe(article.id)
    }

  }

  it should "return empty array of articles when requested user have not created any articles" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      author <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      _ <- articleTestHelper.create[ArticleWithTags](newArticle, author.token)

      response <- articleTestHelper.findAll[WSResponse](ArticlesByAuthor(limit = 5L, offset = 0L, author = Username("not existing username")))
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

      response <- articleTestHelper.findAll[WSResponse](ArticlesByTag(limit = 5L, offset = 0L, tag = newArticle.tagList.head))
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

      response <- articleTestHelper.findAll[WSResponse](ArticlesByTag(limit = 5L, offset = 0L, tag = Tags.dragons.name))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(0L)
    }
  }

  it should "return article favorited by requested user" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      author <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      userWhoFavoritesTheArticle <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.kopernikRegistration)

      article <- articleTestHelper.create[ArticleWithTags](newArticle, author.token)
      _ <- articleTestHelper.favorite[WSResponse](article.slug, userWhoFavoritesTheArticle.token)

      response <- articleTestHelper.findAll[WSResponse](ArticlesByFavorited(limit = 5L, offset = 0L, favoritedBy = userWhoFavoritesTheArticle.username))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(1L)
    }
  }

  it should "not return article, which was not favorited by requested user" in await {
    val newArticle = Articles.hotToTrainYourDragon
    for {
      author <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)
      userWhoFavoritesTheArticle <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.kopernikRegistration)

      article <- articleTestHelper.create[ArticleWithTags](newArticle, author.token)
      _ <- articleTestHelper.favorite[WSResponse](article.slug, userWhoFavoritesTheArticle.token)

      response <- articleTestHelper.findAll[WSResponse](ArticlesByFavorited(limit = 5L, offset = 0L, favoritedBy = author.username))
    } yield {
      response.status.mustBe(OK)
      val page = response.json.as[ArticlePage]
      page.articlesCount.mustBe(0L)
    }
  }

}
