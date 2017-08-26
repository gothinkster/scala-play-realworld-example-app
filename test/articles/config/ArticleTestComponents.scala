package articles.config

import articles.ArticleComponents
import articles.models.{Article, ArticleId}
import articles.repositories.ArticleRepo
import commons.repositories.ActionRunner
import testhelpers.TestUtils

import scala.concurrent.duration.DurationInt

trait ArticleTestComponents {
  _: ArticleComponents =>

  lazy val articlePopulator: ArticlePopulator = new ArticlePopulator(articleRepo, actionRunner)

}

class ArticlePopulator(articleRepo: ArticleRepo,
                       implicit private val actionRunner: ActionRunner) {

  def save(article: Article): Article = {
    val action = articleRepo.create(article)
    TestUtils.runAndAwaitResult(action)(actionRunner, new DurationInt(1).seconds)
  }
}

object Articles {
  val hotToTrainYourDragon: Article = Article(
    ArticleId(-1),
    "how-to-train-your-dragon",
    "how-to-train-your-dragon",
    "Ever wonder how?",
    "It takes a Jacobian",
    null,
    null
  )
}
