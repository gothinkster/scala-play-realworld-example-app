package users.repositories

import commons.models.Username
import slick.dbio.DBIO
import users.models.{Profile, _}

import scala.concurrent.ExecutionContext

class ProfileRepo(userRepo: UserRepo,
                  followAssociationRepo: FollowAssociationRepo,
                  implicit private val ec: ExecutionContext) {

  def getProfileByUserId(userIds: Iterable[UserId], maybeUserId: Option[UserId]): DBIO[Map[UserId, Profile]] = {
    require(userIds != null && maybeUserId != null)

    findByUserIds(userIds, maybeUserId)
      .map(_.map(profile => (profile.userId, profile)).toMap)
  }

  private def findByUserIds(userIds: Iterable[UserId], maybeUserId: Option[UserId]): DBIO[Seq[Profile]] = {
    require(userIds != null && maybeUserId != null)

    for {
      users <- userRepo.findByIds(userIds)
      followAssociations <- getFollowAssociations(userIds, maybeUserId)
    } yield {
      val isFollowing = isFollowingGenerator(followAssociations)(_)
      users.map(user => Profile(user, isFollowing(user.id)))
    }
  }

  private def getFollowAssociations(userIds: Iterable[UserId], maybeUserId: Option[UserId]) = {
    maybeUserId.map(currentUserId => followAssociationRepo.findByFollowerAndFollowed(currentUserId, userIds))
      .getOrElse(DBIO.successful(Seq.empty))
  }

  private def isFollowingGenerator(followAssociations: Seq[FollowAssociation])(userId: UserId): Boolean = {
    val followedIds = followAssociations.map(_.followedId).toSet
    followedIds.contains(userId)
  }

  def findByUsername(username: Username, maybeUserId: Option[UserId]): DBIO[Profile] = {
    require(username != null && maybeUserId != null)

    for {
      user <- userRepo.findByUsername(username)
      profile <- findByUserId(user.id, maybeUserId)
    } yield profile
  }

  def findByUserId(userId: UserId, maybeUserId: Option[UserId]): DBIO[Profile] = {
    require(userId != null && maybeUserId != null)

    findByUserIds(Set(userId), maybeUserId)
      .map(profiles => profiles.head)
  }

}


