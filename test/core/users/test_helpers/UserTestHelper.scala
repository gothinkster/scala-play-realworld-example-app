package core.users.test_helpers

import commons.models.Username
import commons.repositories.ActionRunner
import core.users.models.User
import core.users.repositories.UserRepo
import testhelpers.TestUtils

class UserTestHelper(userRepo: UserRepo,
                     implicit private val actionRunner: ActionRunner) {

  def byLogin(username: Username): Option[User] = {
    val action = userRepo.byUsername(username)
    TestUtils.runAndAwaitResult(action)
  }

}