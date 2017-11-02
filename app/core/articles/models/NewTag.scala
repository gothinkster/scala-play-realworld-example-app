package core.articles.models

case class NewTag(name: String) {
  def toTag: Tag = {
    Tag(TagId(-1), name)
  }
}