package core.users.test_helpers

import commons.models.Login
import commons.repositories.ActionRunner
import core.users.models.User
import core.users.repositories.UserRepo
import testhelpers.TestUtils

class UserTestHelper(userRepo: UserRepo,
                     implicit private val actionRunner: ActionRunner) {

  def byLogin(login: Login): Option[User] = {
    val action = userRepo.byLogin(login)
    TestUtils.runAndAwaitResult(action)
  }

}