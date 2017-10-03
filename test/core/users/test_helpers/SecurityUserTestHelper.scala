package core.users.test_helpers

import commons.models.Login
import commons.repositories.ActionRunner
import core.authentication.api.{SecurityUser, SecurityUserProvider}
import testhelpers.TestUtils

class SecurityUserTestHelper(securityUserProvider: SecurityUserProvider,
                              implicit private val actionRunner: ActionRunner) {

  def byLogin(login: Login): Option[SecurityUser] = {
    val action = securityUserProvider.byLogin(login)
    TestUtils.runAndAwaitResult(action)
  }

}
