package core.users.services

import commons.models.{Email, Username}
import commons.repositories.DateTimeProvider
import commons.utils.DbioUtils
import core.authentication.api._
import core.users.exceptions.MissingUserException
import core.users.models.{FollowAssociation, FollowAssociationId, Profile, User}
import core.users.repositories.{FollowAssociationRepo, ProfileRepo, UserRepo}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

private[users] class ProfileService(userRepo: UserRepo,
                                    followAssociationRepo: FollowAssociationRepo,
                                    securityUserProvider: SecurityUserProvider,
                                    securityUserUpdater: SecurityUserUpdater,
                                    dateTimeProvider: DateTimeProvider,
                                    userUpdateValidator: UserUpdateValidator,
                                    profileRepo: ProfileRepo,
                                    implicit private val ec: ExecutionContext) {

  def unfollow(followedUsername: Username, followerEmail: Email): DBIO[Profile] = {
    require(followedUsername != null && followerEmail != null)

    for {
      follower <- userRepo.findByEmail(followerEmail)
      maybeFollowed <- userRepo.findByUsername(followedUsername)
      followed <- DbioUtils.optionToDbio(maybeFollowed, new MissingUserException(followedUsername))
      _ <- deleteFollowAssociation(follower, followed)
    } yield Profile(followed, following = false)
  }

  private def deleteFollowAssociation(follower: User, followed: User) = {
    followAssociationRepo.findByFollowerAndFollowed(follower.id, followed.id)
      .map(_.map(followAssociation => followAssociationRepo.delete(followAssociation.id)))
  }

  def follow(followedUsername: Username, followerEmail: Email): DBIO[Profile] = {
    require(followedUsername != null && followerEmail != null)

    for {
      follower <- userRepo.findByEmail(followerEmail)
      maybeFollowed <- userRepo.findByUsername(followedUsername)
      followed <- DbioUtils.optionToDbio(maybeFollowed, new MissingUserException(followedUsername))
      _ <- createFollowAssociation(follower, followed)
    } yield Profile(followed, following = true)
  }

  private def createFollowAssociation(follower: User, followed: User) = {
    followAssociationRepo.findByFollowerAndFollowed(follower.id, followed.id)
      .flatMap(maybeFollowAssociation =>
        if (maybeFollowAssociation.isDefined) DBIO.successful(())
        else {
          val followAssociation = FollowAssociation(FollowAssociationId(-1), follower.id, followed.id)
          followAssociationRepo.insert(followAssociation)
        })
  }

  def findByUsername(username: Username, userContext: Option[Email]): DBIO[Profile] = {
    require(username != null && userContext != null)

    profileRepo.findByUsername(username, userContext)
  }

}