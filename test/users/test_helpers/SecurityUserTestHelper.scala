package users.test_helpers

import commons.models.Email
import commons.services.ActionRunner
import authentication.api.SecurityUserProvider
import authentication.models.SecurityUser
import testhelpers.TestUtils

import scala.concurrent.duration.Duration

class SecurityUserTestHelper(securityUserProvider: SecurityUserProvider,
                              implicit private val actionRunner: ActionRunner) {

  def byEmail(email: Email)(implicit duration: Duration): Option[SecurityUser] = {
    val eventualSecurityUser = securityUserProvider.findByEmailOption(email)
    TestUtils.runAndAwaitResult(eventualSecurityUser)
  }

}
