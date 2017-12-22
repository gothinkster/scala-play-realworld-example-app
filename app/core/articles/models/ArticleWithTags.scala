package core.articles.models

import java.time.Instant

import core.users.models.User
import play.api.libs.json.{Format, Json}

case class ArticleWithTags(id: ArticleId,
                            slug: String,
                            title: String,
                            description: String,
                            body: String,
                            createdAt: Instant,
                            updatedAt: Instant,
                            tags: Seq[String],
                            author: User)

object ArticleWithTags {

  implicit val articleWithTagsFormat: Format[ArticleWithTags] = Json.format[ArticleWithTags]

  def apply(article: Article, tags: Seq[Tag], author: User): ArticleWithTags = {
    val tagValuesSorted = tags.map(_.name).sorted
    ArticleWithTags(
      article.id,
      article.slug,
      article.title,
      article.description,
      article.body,
      article.createdAt,
      article.updatedAt,
      tagValuesSorted,
      author
    )
  }

  def fromTagValues(article: Article, tagValues: Seq[String], author: User): ArticleWithTags = {
    val tagValuesSorted = tagValues.sorted
    ArticleWithTags(
      article.id,
      article.slug,
      article.title,
      article.description,
      article.body,
      article.createdAt,
      article.updatedAt,
      tagValuesSorted,
      author
    )
  }
}