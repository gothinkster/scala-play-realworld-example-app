
package core.users.models

import java.time.Instant

import commons.models.{IdMetaModel, Property}
import commons.repositories.{BaseId, WithId}
import play.api.libs.json._
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class FollowAssociation(id: FollowAssociationId, followerId: UserId, followedId: UserId)
  extends WithId[Long, FollowAssociationId]

object FollowAssociation {
  implicit val followAssociationFormat: Format[FollowAssociation] = Json.format[FollowAssociation]
}

case class FollowAssociationId(value: Long) extends AnyVal with BaseId[Long]

object FollowAssociationId {

  implicit val followAssociationIdFormat: Format[FollowAssociationId] = new Format[FollowAssociationId] {
    override def reads(json: JsValue): JsResult[FollowAssociationId] =
      Reads.LongReads.reads(json).map(FollowAssociationId(_))

    override def writes(o: FollowAssociationId): JsNumber = Writes.LongWrites.writes(o.value)
  }

  implicit val followAssociationIdDbMapping: BaseColumnType[FollowAssociationId] =
    MappedColumnType.base[FollowAssociationId, Long](
      vo => vo.value,
      id => FollowAssociationId(id)
    )
}

object FollowAssociationMetaModel extends IdMetaModel {

  val followerId: Property[UserId] = Property("followerId")
  val followedId: Property[UserId] = Property("followedId")
  val updatedAt: Property[Instant] = Property("updatedAt")

  override type ModelId = FollowAssociationId
}