package commons.models

case class PageRequest(tag: Option[String], author: Option[Username], favorited: Option[String],
                       limit: Long, offset: Long, orderings: List[Ordering])