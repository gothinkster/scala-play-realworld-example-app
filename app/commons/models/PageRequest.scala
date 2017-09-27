package commons.models

case class PageRequest(limit: Long, offset: Long, orderings: List[Ordering])