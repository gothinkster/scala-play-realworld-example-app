
package users.repositories

import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}
import users.models.{FollowAssociation, FollowAssociationId, UserId}

import scala.concurrent.ExecutionContext

class FollowAssociationRepo(implicit private val ec: ExecutionContext) {
  import FollowAssociationTable.followAssociations

  def findByFollower(followerId: UserId): DBIO[Seq[FollowAssociation]] = {
    followAssociations
      .filter(_.followerId === followerId)
      .result
  }

  def findByFollowerAndFollowed(followerId: UserId, followedId: UserId): DBIO[Option[FollowAssociation]] = {
    getFollowerAndFollowedQuery(followerId, Seq(followedId)).result.headOption
  }

  def findByFollowerAndFollowed(followerId: UserId, followedIds: Iterable[UserId]): DBIO[Seq[FollowAssociation]] = {
    getFollowerAndFollowedQuery(followerId, followedIds).result
  }

  private def getFollowerAndFollowedQuery(followerId: UserId, followedIds: Iterable[UserId]) = {
    followAssociations
      .filter(_.followerId === followerId)
      .filter(_.followedId inSet followedIds)
  }

  def delete(id: FollowAssociationId): DBIO[Int] = {
    require(id != null)

    followAssociations
      .filter(_.id === id)
      .delete
  }

  def insert(followAccociation: FollowAssociation): DBIO[FollowAssociationId] = {
    require(followAccociation != null)

    followAssociations
      .returning(followAssociations.map(_.id)) += followAccociation
  }
}

object FollowAssociationTable {
  val followAssociations = TableQuery[FollowAssociations]

  protected class FollowAssociations(tag: Tag) extends Table[FollowAssociation](tag, "follow_associations") {

    def id: Rep[FollowAssociationId] = column[FollowAssociationId]("id", O.PrimaryKey, O.AutoInc)

    def followerId: Rep[UserId] = column("follower_id")

    def followedId: Rep[UserId] = column("followed_id")

    def * : ProvenShape[FollowAssociation] = (id, followerId, followedId) <> ((FollowAssociation.apply _).tupled,
      FollowAssociation.unapply)
  }
}
