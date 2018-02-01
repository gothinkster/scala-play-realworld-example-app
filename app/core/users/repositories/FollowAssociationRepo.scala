
package core.users.repositories

import commons.models.{IdMetaModel, Property}
import commons.repositories._
import core.users.models.{FollowAssociation, FollowAssociationId, FollowAssociationMetaModel, UserId}
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class FollowAssociationRepo(implicit private val ec: ExecutionContext)
  extends BaseRepo[FollowAssociationId, FollowAssociation, FollowAssociationTable] {

  def byFollowerAndFollowed(followerId: UserId, followedId: UserId): DBIO[Option[FollowAssociation]] = {
    query
      .filter(table => table.followerId === followerId && table.followedId === followedId)
      .result
      .headOption
  }

  override protected val mappingConstructor: Tag => FollowAssociationTable = new FollowAssociationTable(_)

  override protected val modelIdMapping: BaseColumnType[FollowAssociationId] =
    FollowAssociationId.followAssociationIdDbMapping

  override protected val metaModel: IdMetaModel = FollowAssociationMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (FollowAssociationTable) => Rep[_]] = Map(
    FollowAssociationMetaModel.id -> (table => table.id),
    FollowAssociationMetaModel.followerId -> (table => table.followerId),
    FollowAssociationMetaModel.followedId -> (table => table.followedId),
  )
}

protected class FollowAssociationTable(tag: Tag)
  extends IdTable[FollowAssociationId, FollowAssociation](tag, "follow_associations") {

  def followerId: Rep[UserId] = column("follower_id")

  def followedId: Rep[UserId] = column("followed_id")

  def * : ProvenShape[FollowAssociation] = (id, followerId, followedId) <> ((FollowAssociation.apply _).tupled,
    FollowAssociation.unapply)
}