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
                           tagList: Seq[String],
                           author: User,
                           favorited: Boolean,
                           favoritesCount: Int)

object ArticleWithTags {

  implicit val articleWithTagsFormat: Format[ArticleWithTags] = Json.format[ArticleWithTags]

  def apply(article: Article, tags: Seq[Tag], author: User, favorited: Boolean,
            favoritesCount: Int): ArticleWithTags = {
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
      author,
      favorited,
      favoritesCount
    )
  }

  def fromTagValues(article: Article, tagValues: Seq[String], author: User, favorited: Boolean,
                    favoritesCount: Int): ArticleWithTags = {
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
      author,
      favorited,
      favoritesCount
    )
  }
}