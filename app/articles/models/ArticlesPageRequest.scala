package articles.models

import commons.models.Username

sealed trait ArticlesPageRequest {
  def limit: Long
  def offset: Long
}

case class ArticlesAll(limit: Long, offset: Long) extends ArticlesPageRequest
case class ArticlesByTag(tag: String, limit: Long, offset: Long) extends ArticlesPageRequest
case class ArticlesByAuthor(author: Username, limit: Long, offset: Long) extends ArticlesPageRequest
case class ArticlesByFavorited(favoritedBy: Username, limit: Long, offset: Long) extends ArticlesPageRequest