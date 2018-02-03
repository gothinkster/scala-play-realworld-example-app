
package core.users.models

import commons.models.{IdMetaModel, Property}
import commons.repositories.{BaseId, WithId}
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class FollowAssociation(id: FollowAssociationId, followerId: UserId, followedId: UserId)
  extends WithId[Long, FollowAssociationId]

case class FollowAssociationId(value: Long) extends AnyVal with BaseId[Long]

object FollowAssociationId {

  implicit val followAssociationIdDbMapping: BaseColumnType[FollowAssociationId] =
    MappedColumnType.base[FollowAssociationId, Long](
      vo => vo.value,
      id => FollowAssociationId(id)
    )

}

object FollowAssociationMetaModel extends IdMetaModel {

  val followerId: Property[UserId] = Property("followerId")
  val followedId: Property[UserId] = Property("followedId")

  override type ModelId = FollowAssociationId
}