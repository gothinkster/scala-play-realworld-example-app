package commons.models


trait IdMetaModel {
  type ModelId <: BaseId[_]

  val id: Property[Option[ModelId]] = Property("id")
}
