package core.articles.exceptions

class MissingArticleException(slug: String) extends RuntimeException(s"missing with slug: $slug")