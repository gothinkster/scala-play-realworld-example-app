package commons.models

import commons.repositories.BaseId


trait IdMetaModel {
  type ModelId <: BaseId[_]

  val id: Property[Option[ModelId]] = Property("id")
}
