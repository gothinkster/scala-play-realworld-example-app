package users.services

import authentication.api._
import commons.models.Username
import commons.repositories.DateTimeProvider
import slick.dbio.DBIO
import users.models.{FollowAssociation, FollowAssociationId, Profile, UserId}
import users.repositories.{FollowAssociationRepo, ProfileRepo, UserRepo}

import scala.concurrent.ExecutionContext

private[users] class ProfileService(userRepo: UserRepo,
                                    followAssociationRepo: FollowAssociationRepo,
                                    securityUserProvider: SecurityUserProvider,
                                    securityUserUpdater: SecurityUserUpdater,
                                    dateTimeProvider: DateTimeProvider,
                                    userUpdateValidator: UserUpdateValidator,
                                    profileRepo: ProfileRepo,
                                    implicit private val ec: ExecutionContext) {

  def unfollow(followedUsername: Username, followerId: UserId): DBIO[Profile] = {
    require(followedUsername != null && followerId != null)

    for {
      followed <- userRepo.findByUsername(followedUsername)
      _ <- deleteFollowAssociation(followerId, followed.id)
    } yield Profile(followed, following = false)
  }

  private def deleteFollowAssociation(followerId: UserId, followedId: UserId) = {
    followAssociationRepo.findByFollowerAndFollowed(followerId, followedId)
      .map(_.map(followAssociation => followAssociationRepo.delete(followAssociation.id)))
  }

  def follow(followedUsername: Username, followerId: UserId): DBIO[Profile] = {
    require(followedUsername != null && followerId != null)

    for {
      followed <- userRepo.findByUsername(followedUsername)
      _ <- createFollowAssociation(followerId, followed.id)
    } yield Profile(followed, following = true)
  }

  private def createFollowAssociation(followerId: UserId, followedId: UserId) = {
    followAssociationRepo.findByFollowerAndFollowed(followerId, followedId)
      .flatMap(maybeFollowAssociation =>
        if (maybeFollowAssociation.isDefined) DBIO.successful(())
        else {
          val followAssociation = FollowAssociation(FollowAssociationId(-1), followerId, followedId)
          followAssociationRepo.insert(followAssociation)
        })
  }

  def findByUsername(username: Username, maybeUserId: Option[UserId]): DBIO[Profile] = {
    require(username != null && maybeUserId != null)

    profileRepo.findByUsername(username, maybeUserId)
  }

}