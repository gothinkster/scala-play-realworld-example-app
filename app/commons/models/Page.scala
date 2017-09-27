package commons.models

case class Page[Model](models: Seq[Model], count: Long)