package users.test_helpers

import commons.models.Email
import commons.services.ActionRunner
import authentication.api.{SecurityUser, SecurityUserProvider}
import testhelpers.TestUtils

import scala.concurrent.duration.Duration

class SecurityUserTestHelper(securityUserProvider: SecurityUserProvider,
                              implicit private val actionRunner: ActionRunner) {

  def byEmail(email: Email)(implicit duration: Duration): Option[SecurityUser] = {
    val eventualSecurityUser = securityUserProvider.findByEmail(email)
    TestUtils.runAndAwaitResult(eventualSecurityUser)
  }

}
