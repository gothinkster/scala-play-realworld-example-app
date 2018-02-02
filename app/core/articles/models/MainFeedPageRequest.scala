package core.articles.models

import commons.models.{Ordering, Username}

case class MainFeedPageRequest(tag: Option[String], author: Option[Username], favorited: Option[String],
                               limit: Long, offset: Long, orderings: List[Ordering])