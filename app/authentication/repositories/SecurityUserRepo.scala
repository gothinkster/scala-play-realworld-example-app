package authentication.repositories

import authentication.models.{PasswordHash, SecurityUser, SecurityUserId}
import commons.models.{IdMetaModel, Login, Property}
import commons.repositories._
import commons.repositories.mappings.{JavaTimeDbMappings, LoginDbMappings}
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

private[authentication] class SecurityUserRepo(
                                                         override protected val dateTimeProvider: DateTimeProvider)
  extends BaseRepo[SecurityUserId, SecurityUser, SecurityUserTable]
    with AuditDateTimeRepo[SecurityUserId, SecurityUser, SecurityUserTable]
    with LoginDbMappings
    with SecurityUserDbMappings {

  def byLogin(login: Login): DBIO[Option[SecurityUser]] = {
    query
      .filter(_.login === login)
      .result
      .headOption
  }

  override protected val mappingConstructor: Tag => SecurityUserTable = new SecurityUserTable(_)

  override protected val modelIdMapping: BaseColumnType[SecurityUserId] = MappedColumnType.base[SecurityUserId, Long](
    vo => vo.value,
    id => SecurityUserId(id)
  )

  override protected val metaModel: IdMetaModel = SecurityUserMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (SecurityUserTable) => Rep[_]] = Map(
    SecurityUserMetaModel.id -> (table => table.id),
    SecurityUserMetaModel.login -> (table => table.login),
    SecurityUserMetaModel.password -> (table => table.password)
  )

}

protected class SecurityUserTable(tag: Tag) extends IdTable[SecurityUserId, SecurityUser](tag, "security_user")
  with AuditDateTimeTable
  with JavaTimeDbMappings
  with LoginDbMappings
  with SecurityUserDbMappings {

  def login: Rep[Login] = column("login")

  def password: Rep[PasswordHash] = column("password")

  def * : ProvenShape[SecurityUser] = (id, login, password, createdAt, modifiedAt) <> (SecurityUser.tupled,
    SecurityUser.unapply)
}

private[authentication] object SecurityUserMetaModel extends IdMetaModel {
  override type ModelId = SecurityUserId

  val login: Property[Login] = Property("login")
  val password: Property[PasswordHash] = Property("password")
}


