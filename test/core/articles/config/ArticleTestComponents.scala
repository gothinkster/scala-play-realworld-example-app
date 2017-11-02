package core.articles.config

import core.articles.ArticleComponents
import core.articles.models.{Article, NewArticle, NewTag, Tag}
import core.articles.repositories.{ArticleRepo, TagRepo}
import commons.repositories.ActionRunner
import testhelpers.TestUtils
import core.users.models.UserId

import scala.concurrent.duration.DurationInt

trait ArticleTestComponents {
  _: ArticleComponents =>

  lazy val articlePopulator: ArticlePopulator = new ArticlePopulator(articleRepo, actionRunner)

  lazy val tagPopulator: TagPopulator = new TagPopulator(tagRepo, actionRunner)

}

class ArticlePopulator(articleRepo: ArticleRepo,
                       implicit private val actionRunner: ActionRunner) {

  def save(article: NewArticle): Article = {
    val action = articleRepo.create(article.toArticle)
    TestUtils.runAndAwaitResult(action)(actionRunner, new DurationInt(1).minute)
  }

}

class TagPopulator(tagRepo: TagRepo,
                   implicit private val actionRunner: ActionRunner) {

  def save(tag: NewTag): Tag = {
    val action = tagRepo.create(tag.toTag)
    TestUtils.runAndAwaitResult(action)(actionRunner, new DurationInt(1).minute)
  }

}

object Articles {
  val hotToTrainYourDragon: NewArticle = NewArticle(
    "how-to-train-your-dragon",
    "how-to-train-your-dragon",
    "Ever wonder how?",
    "It takes a Jacobian",
    UserId(-1)
  )
}

object Tags {
  val dragons: NewTag = NewTag("dragons")
}
