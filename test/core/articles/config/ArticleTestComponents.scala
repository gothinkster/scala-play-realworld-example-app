package core.articles.config

import commons.repositories.ActionRunner
import core.articles.ArticleComponents
import core.articles.models._
import core.articles.repositories.{ArticleRepo, ArticleTagRepo, TagRepo}
import core.users.models.UserId
import testhelpers.Populator

trait ArticleTestComponents {
  _: ArticleComponents =>

  lazy val articlePopulator: ArticlePopulator = new ArticlePopulator(articleRepo, actionRunner)

  lazy val tagPopulator: TagPopulator = new TagPopulator(tagRepo, actionRunner)

  lazy val articleTagPopulator: ArticleTagPopulator = new ArticleTagPopulator(articleTagRepo, actionRunner)

}

class ArticlePopulator(articleRepo: ArticleRepo,
                       implicit private val actionRunner: ActionRunner) extends Populator {

  def save(article: NewArticle): Article = {
    runAndAwait(articleRepo.create(article.toArticle))
  }

}

class TagPopulator(tagRepo: TagRepo,
                   implicit private val actionRunner: ActionRunner) extends Populator {

  def save(tag: NewTag): Tag = {
    runAndAwait(tagRepo.create(tag.toTag))
  }

}

class ArticleTagPopulator(articleTagRepo: ArticleTagRepo,
                          implicit private val actionRunner: ActionRunner) extends Populator {

  def save(articleTag: ArticleTag): ArticleTag = {
    runAndAwait(articleTagRepo.create(articleTag))
  }

}

object Articles {
  val hotToTrainYourDragon: NewArticle = NewArticle(
    "how-to-train-your-dragon",
    "how-to-train-your-dragon",
    "Ever wonder how?",
    "It takes a Jacobian",
    UserId(-1),
    Seq(Tags.dragons.name)
  )
}

object Tags {
  val dragons: NewTag = NewTag("dragons")
}
