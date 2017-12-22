package core.articles.config

import commons.repositories.{ActionRunner, DateTimeProvider}
import core.articles.ArticleComponents
import core.articles.models._
import core.articles.repositories.{ArticleRepo, ArticleTagRepo, TagRepo}
import core.users.config.UserTestComponents
import core.users.models.User
import core.users.test_helpers.UserPopulator
import testhelpers.Populator

trait ArticleTestComponents {
  _: ArticleComponents with UserTestComponents =>

  lazy val articlePopulator: ArticlePopulator =
    new ArticlePopulator(articleRepo, userPopulator, dateTimeProvider, actionRunner)

  lazy val tagPopulator: TagPopulator = new TagPopulator(tagRepo, actionRunner)

  lazy val articleTagPopulator: ArticleTagPopulator = new ArticleTagPopulator(articleTagRepo, actionRunner)

}

class ArticlePopulator(articleRepo: ArticleRepo,
                       userPopulator: UserPopulator,
                       dateTimeProvider: DateTimeProvider,
                       implicit private val actionRunner: ActionRunner) extends Populator {

  def save(article: NewArticle)(user: User): Article = {
    runAndAwait(articleRepo.create(article.toArticle(user.id, dateTimeProvider)))
  }

}

class TagPopulator(tagRepo: TagRepo,
                   implicit private val actionRunner: ActionRunner) extends Populator {

  def all: Seq[Tag] = {
    runAndAwait(tagRepo.all)
  }

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
    Seq(Tags.dragons.name)
  )
}

object Tags {
  val dragons: NewTag = NewTag("dragons")
}
