package core.users.models

import commons.models.{Email, IdMetaModel, Login, Property}
import commons.repositories.{BaseId, WithId}

case class User(id: UserId, login: Login, email: Email) extends WithId[Long, UserId]

case class UserId(override val id: Long) extends AnyVal with BaseId[Long]

object UserMetaModel extends IdMetaModel {
  override type ModelId = UserId

  val login: Property[Login] = Property("login")
  val email: Property[Email] = Property("email")
}
