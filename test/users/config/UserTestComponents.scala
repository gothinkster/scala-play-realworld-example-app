package users.config

import commons.services.ActionRunner
import authentication.AuthenticationComponents
import users.UserComponents
import users.models.FollowAssociation
import users.repositories.FollowAssociationRepo
import users.test_helpers.{SecurityUserTestHelper, UserPopulator, UserRegistrationTestHelper, UserTestHelper}
import testhelpers.Populator

trait UserTestComponents {
  _: AuthenticationComponents with UserComponents =>

  lazy val userRegistrationTestHelper: UserRegistrationTestHelper =
    new UserRegistrationTestHelper(userRegistrationService, "/users/login", actionRunner)

  lazy val securityUserTestHelper: SecurityUserTestHelper =
    new SecurityUserTestHelper(securityUserProvider, actionRunner)

  lazy val userTestHelper: UserTestHelper = new UserTestHelper(userRepo, actionRunner)

  lazy val userPopulator: UserPopulator = new UserPopulator(userRepo, actionRunner)

  lazy val followAssociationTestHelper: FollowAssociationTestHelper =
    new FollowAssociationTestHelper(followAssociationRepo, actionRunner)
}

class FollowAssociationTestHelper(followAssociationRepo: FollowAssociationRepo,
                                  implicit private val actionRunner: ActionRunner) extends Populator {

  def save(followAssociation: FollowAssociation): FollowAssociation = {
    runAndAwait(followAssociationRepo.insertAndGet(followAssociation))
  }

}