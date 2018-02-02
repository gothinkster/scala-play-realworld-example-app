package core.articles.models

import commons.models.Ordering

case class UserFeedPageRequest(limit: Long, offset: Long, orderings: List[Ordering])