package articles.models

import commons.models.{Ordering, Username}

case class MainFeedPageRequest(tag: Option[String] = None,
                               author: Option[Username] = None,
                               favorited: Option[Username] = None,
                               limit: Long,
                               offset: Long,
                               orderings: List[Ordering] = Nil)