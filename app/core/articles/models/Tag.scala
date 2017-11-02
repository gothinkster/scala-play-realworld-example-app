package core.articles.models

import commons.models.{IdMetaModel, Property}
import commons.repositories.{BaseId, WithId}

case class Tag(id: TagId,
               name: String) extends WithId[Long, TagId]

case class TagId(override val id: Long) extends AnyVal with BaseId[Long]

object TagMetaModel extends IdMetaModel {
  val name: Property[String] = Property("name")

  override type ModelId = TagId
}