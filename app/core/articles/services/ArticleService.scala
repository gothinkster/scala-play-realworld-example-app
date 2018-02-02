package core.articles.services

import com.github.slugify.Slugify
import commons.models.{Email, Page}
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import core.articles.exceptions.MissingArticleException
import core.articles.models._
import core.articles.repositories.{ArticleRepo, ArticleTagRepo, ArticleWithTagsRepo, TagRepo}
import core.users.models.User
import org.apache.commons.lang3.StringUtils
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ArticleService(articleRepo: ArticleRepo,
                     articleTagRepo: ArticleTagRepo,
                     tagRepo: TagRepo,
                     dateTimeProvider: DateTimeProvider,
                     articleWithTagsRepo: ArticleWithTagsRepo,
                     implicit private val ex: ExecutionContext) {

  def bySlug(slug: String): DBIO[ArticleWithTags] = {
    require(StringUtils.isNotBlank(slug))

    for {
      maybeArticleWithAuthor <- articleRepo.bySlugWithAuthor(slug)
      (article, user) <- DbioUtils.optionToDbio(maybeArticleWithAuthor, new MissingArticleException(slug))
      tags <- articleTagRepo.byArticleId(article.id)
    } yield ArticleWithTags(article, tags, user)
  }

  def create(newArticle: NewArticle, user: User): DBIO[ArticleWithTags] = {
    require(newArticle != null && user != null)

    val article = createArticle(newArticle, user)

    for {
      articleId <- articleRepo.insert(article)
      (article, user) <- articleRepo.byIdWithUser(articleId)
      tags <- createTagsIfNotExist(newArticle)
      _ <- associateTagsWithArticle(article, tags)
    } yield ArticleWithTags(article, tags, user)
  }

  private def createArticle(newArticle: NewArticle, user: User) = {
    val slugifier = new Slugify()
    val slug = slugifier.slugify(newArticle.title)
    newArticle.toArticle(slug, user.id, dateTimeProvider)
  }

  private def associateTagsWithArticle(article: Article, tags: Seq[Tag]) = {
    val articleTags = tags.map(tag => ArticleTag.from(article, tag))

    articleTagRepo.create(articleTags)
  }

  private def createTagsIfNotExist(newArticle: NewArticle) = {
    val tagNames = newArticle.tagList
    val tags = tagNames.map(Tag.from)

    tagRepo.createIfNotExist(tags)
  }

  def all(pageRequest: MainFeedPageRequest): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null)

    articleWithTagsRepo.all(pageRequest)
  }

  def feed(pageRequest: UserFeedPageRequest, followerEmail: Email): DBIO[Page[ArticleWithTags]] = {
    require(pageRequest != null && followerEmail != null)

    articleWithTagsRepo.feed(pageRequest, followerEmail)
  }

}
