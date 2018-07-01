package users.test_helpers

import commons.models.Username
import commons.services.ActionRunner
import users.models.User
import users.repositories.UserRepo
import testhelpers.TestUtils

class UserTestHelper(userRepo: UserRepo,
                     implicit private val actionRunner: ActionRunner) {

  def byLogin(username: Username): Option[User] = {
    val action = userRepo.findByUsernameOption(username)
    TestUtils.runAndAwaitResult(action)
  }

}