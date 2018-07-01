package users.repositories

import commons.models.{Email, Username}
import users.models.{Profile, _}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ProfileRepo(userRepo: UserRepo,
                  followAssociationRepo: FollowAssociationRepo,
                  implicit private val ec: ExecutionContext) {

  def getProfileByUserId(userIds: Iterable[UserId], maybeCurrentUserEmail: Option[Email]): DBIO[Map[UserId, Profile]] = {
    require(userIds != null && maybeCurrentUserEmail != null)

    findByUserIds(userIds, maybeCurrentUserEmail)
      .map(_.map(profile => (profile.userId, profile)).toMap)
  }

  private def findByUserIds(userIds: Iterable[UserId], maybeCurrentUserEmail: Option[Email]): DBIO[Seq[Profile]] = {
    require(userIds != null && maybeCurrentUserEmail != null)

    for {
      users <- userRepo.findByIds(userIds)
      followAssociations <- getFollowAssociations(userIds, maybeCurrentUserEmail)
    } yield {
      val isFollowing = isFollowingGenerator(followAssociations)(_)
      users.map(user => Profile(user, isFollowing(user.id)))
    }
  }

  private def getFollowAssociations(userIds: Iterable[UserId], maybeCurrentUserEmail: Option[Email]) = {
    maybeCurrentUserEmail.map(email => userRepo.findByEmail(email))
      .map(_.flatMap(currentUser => followAssociationRepo.findByFollowerAndFollowed(currentUser.id, userIds)))
      .getOrElse(DBIO.successful(Seq.empty))
  }

  private def isFollowingGenerator(followAssociations: Seq[FollowAssociation])(userId: UserId): Boolean = {
    val followedIds = followAssociations.map(_.followedId).toSet
    followedIds.contains(userId)
  }

  def findByUsername(username: Username, maybeCurrentUserEmail: Option[Email]): DBIO[Profile] = {
    require(username != null && maybeCurrentUserEmail != null)

    for {
      user <- userRepo.findByUsername(username)
      profile <- findByUserId(user.id, maybeCurrentUserEmail)
    } yield profile
  }

  def findByUserId(userId: UserId, maybeCurrentUserEmail: Option[Email]): DBIO[Profile] = {
    require(userId != null && maybeCurrentUserEmail != null)

    findByUserIds(Set(userId), maybeCurrentUserEmail)
      .map(profiles => profiles.head)
  }

}


