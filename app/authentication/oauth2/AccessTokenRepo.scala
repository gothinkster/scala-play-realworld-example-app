package authentication.oauth2

import authentication.models._
import commons.models.{IdMetaModel, Property}
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

private[authentication] class AccessTokenRepo(
                                override protected val dateTimeProvider: DateTimeProvider)
  extends BaseRepo[AccessTokenId, AccessToken, AccessTokenTable]
    with AuditDateTimeRepo[AccessTokenId, AccessToken, AccessTokenTable] {

  def bySecurityUserId(securityUserId: SecurityUserId): DBIO[Option[AccessToken]] = {
    query
      .filter(_.securityUserId === securityUserId)
      .result
      .headOption
  }

  def byToken(token: String): DBIO[Option[AccessToken]] = {
    query
      .filter(_.token === token)
      .result
      .headOption
  }


  override protected val mappingConstructor: Tag => AccessTokenTable = new AccessTokenTable(_)

  override protected val modelIdMapping: BaseColumnType[AccessTokenId] = MappedColumnType.base[AccessTokenId, Long](
    vo => vo.value,
    id => AccessTokenId(id)
  )

  override protected val metaModel: IdMetaModel = AccessTokenMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (AccessTokenTable) => Rep[_]] = Map(
    AccessTokenMetaModel.id -> (table => table.id),
    AccessTokenMetaModel.token -> (table => table.token),
    AccessTokenMetaModel.securityUserId -> (table => table.securityUserId)
  )

}

protected class AccessTokenTable(tag: Tag) extends IdTable[AccessTokenId, AccessToken](tag, "access_token")
  with AuditDateTimeTable
  with JavaTimeDbMappings {

  def securityUserId: Rep[SecurityUserId] = column("id_security_user")

  def token: Rep[String] = column("token")

  def * : ProvenShape[AccessToken] = (id, securityUserId, token, createdAt, modifiedAt) <> (AccessToken.tupled,
    AccessToken.unapply)
}

private[authentication] object AccessTokenMetaModel extends IdMetaModel {
  override type ModelId = AccessTokenId

  val token: Property[String] = Property("token")
  val securityUserId: Property[SecurityUserId] = Property("securityUserId")
}
