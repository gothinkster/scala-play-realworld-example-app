package core.users.repositories

import commons.models.{Email, Username}
import commons.utils.DbioUtils
import core.users.exceptions.MissingUserException
import core.users.models.{Profile, _}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

class ProfileRepo(userRepo: UserRepo,
                  followAssociationRepo: FollowAssociationRepo,
                  implicit private val ec: ExecutionContext) {

  def getProfileByUserId(users: Seq[User], maybeCurrentUserEmail: Option[Email]): DBIO[Map[UserId, Profile]] = {
    require(users != null && maybeCurrentUserEmail != null)

    findByUsers(users, maybeCurrentUserEmail)
      .map(_.map(profile => (profile.userId, profile)).toMap)
  }

  def findByUsername(username: Username, maybeCurrentUserEmail: Option[Email]): DBIO[Profile] = {
    require(username != null && maybeCurrentUserEmail != null)

    for {
      maybeUser <- userRepo.findByUsername(username)
      user <- DbioUtils.optionToDbio(maybeUser, new MissingUserException(username))
      profile <- findByUser(user, maybeCurrentUserEmail)
    } yield profile
  }

  def findByUser(user: User, maybeCurrentUserEmail: Option[Email]): DBIO[Profile] = {
    require(user != null && maybeCurrentUserEmail != null)

    findByUsers(Seq(user), maybeCurrentUserEmail)
      .map(profiles => profiles.head)
  }

  private def findByUsers(users: Seq[User], maybeCurrentUserEmail: Option[Email]): DBIO[Seq[Profile]] = {
    require(users != null && maybeCurrentUserEmail != null)

    getFollowAssociations(users, maybeCurrentUserEmail)
      .map(followAssociations => {
        val isFollowing = isFollowingGenerator(followAssociations)(_)
        users.map(user => Profile(user, isFollowing(user.id)))
      })
  }

  private def getFollowAssociations(users: Seq[User], maybeCurrentUserEmail: Option[Email]) = {
    val userIds = users.map(_.id)
    maybeCurrentUserEmail.map(email => userRepo.findByEmail(email))
      .map(_.flatMap(currentUser => followAssociationRepo.findByFollowerAndFollowed(currentUser.id, userIds)))
      .getOrElse(DBIO.successful(Seq.empty))
  }

  private def isFollowingGenerator(followAssociations: Seq[FollowAssociation])(userId: UserId): Boolean = {
    val followedIds = followAssociations.map(_.followedId).toSet
    followedIds.contains(userId)
  }

}


