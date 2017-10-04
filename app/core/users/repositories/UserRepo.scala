package core.users.repositories

import commons.models.{Email, IdMetaModel, Login, Property}
import commons.repositories.mappings.{EmailDbMappings, LoginDbMappings}
import commons.repositories.{BaseRepo, IdTable}
import core.users.models.{User, UserId, UserMetaModel}
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

class UserRepo()
  extends BaseRepo[UserId, User, UserTable] {

  def byLogin(login: Login): DBIO[Option[User]] = {
    query
      .filter(_.login === login)
      .result
      .headOption
  }

  override protected val mappingConstructor: Tag => UserTable = new UserTable(_)

  override protected val modelIdMapping: BaseColumnType[UserId] = MappedColumnType.base[UserId, Long](
    vo => vo.value,
    id => UserId(id)
  )

  override protected val metaModel: IdMetaModel = UserMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (UserTable) => Rep[_]] = Map(
    UserMetaModel.id -> (table => table.id),
    UserMetaModel.login -> (table => table.login)
  )

  implicit val loginMapping: BaseColumnType[Login] = MappedColumnType.base[Login, String](
    login => login.value,
    str => Login(str)
  )

}

protected class UserTable(tag: Tag) extends IdTable[UserId, User](tag, "user")
  with LoginDbMappings
  with EmailDbMappings {

  def login: Rep[Login] = column[Login]("login")

  def email: Rep[Email] = column[Email]("email")

  def * : ProvenShape[User] = (id, login, email) <> (User.tupled, User.unapply)
}