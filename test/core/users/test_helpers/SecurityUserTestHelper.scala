package core.users.test_helpers

import commons.models.Email
import commons.repositories.ActionRunner
import core.authentication.api.{SecurityUser, SecurityUserProvider}
import testhelpers.TestUtils

import scala.concurrent.duration.Duration

class SecurityUserTestHelper(securityUserProvider: SecurityUserProvider,
                              implicit private val actionRunner: ActionRunner) {

  def byEmail(email: Email)(implicit duration: Duration): Option[SecurityUser] = {
    val eventualSecurityUser = securityUserProvider.byEmail(email)
    TestUtils.runAndAwaitResult(eventualSecurityUser)
  }

}
