package core.articles.models

import commons.models.{IdMetaModel, Property}
import commons.repositories.{BaseId, WithId}
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class Tag(id: TagId,
               name: String) extends WithId[Long, TagId]

object Tag {
  def from(tagValue: String): Tag = Tag(TagId(-1), tagValue)
}

case class TagId(override val value: Long) extends AnyVal with BaseId[Long]

object TagId {
  implicit val tagIdDbMapping: BaseColumnType[TagId] = MappedColumnType.base[TagId, Long](
    vo => vo.value,
    id => TagId(id)
  )
}

object TagMetaModel extends IdMetaModel {
  val name: Property[String] = Property("name")

  override type ModelId = TagId
}