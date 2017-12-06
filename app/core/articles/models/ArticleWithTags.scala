package core.articles.models

import java.time.Instant

import play.api.libs.json.{Format, Json}

case class ArticleWithTags(id: ArticleId,
                            slug: String,
                            title: String,
                            description: String,
                            body: String,
                            createdAt: Instant,
                            updatedAt: Instant,
                            tags: Seq[String])

object ArticleWithTags {

  implicit val articleWithTagsFormat: Format[ArticleWithTags] = Json.format[ArticleWithTags]

  def apply(article: Article, tags: Seq[Tag]): ArticleWithTags = {
    val tagValuesSorted = tags.map(_.name).sorted
    ArticleWithTags(
      article.id,
      article.slug,
      article.title,
      article.description,
      article.body,
      article.createdAt,
      article.updatedAt,
      tagValuesSorted
    )
  }

  def fromArticleAndRawTags(article: Article, tagValues: Seq[String]): ArticleWithTags = {
    val tagValuesSorted = tagValues.sorted
    ArticleWithTags(
      article.id,
      article.slug,
      article.title,
      article.description,
      article.body,
      article.createdAt,
      article.updatedAt,
      tagValuesSorted
    )
  }
}