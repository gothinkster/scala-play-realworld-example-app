package core.users.config

import authentication.AuthenticationComponents
import core.users.UserComponents
import core.users.test_helpers.{SecurityUserTestHelper, UserRegistrationTestHelper, UserTestHelper}

trait UserTestComponents {
  _: AuthenticationComponents with UserComponents =>

  lazy val userRegistrationTestHelper: UserRegistrationTestHelper =
    new UserRegistrationTestHelper(userRegistrationService, "/authenticate", actionRunner)

  lazy val securityUserTestHelper: SecurityUserTestHelper =
    new SecurityUserTestHelper(securityUserProvider, actionRunner)

  lazy val userTestHelper: UserTestHelper = new UserTestHelper(userRepo, actionRunner)

}