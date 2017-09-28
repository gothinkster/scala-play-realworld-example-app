package core.users.models

import commons.models.{IdMetaModel, Login, Property}
import commons.repositories.{BaseId, WithId}

case class User(id: UserId, login: Login) extends WithId[Long, UserId]

case class UserId(override val id: Long) extends AnyVal with BaseId[Long]

object UserMetaModel extends IdMetaModel {
  override type ModelId = UserId

  val login: Property[Login] = Property("login")
}
