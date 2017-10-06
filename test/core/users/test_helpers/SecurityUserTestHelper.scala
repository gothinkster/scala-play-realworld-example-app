package core.users.test_helpers

import commons.models.Login
import commons.repositories.ActionRunner
import core.authentication.api.{SecurityUser, SecurityUserProvider}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SecurityUserTestHelper(securityUserProvider: SecurityUserProvider,
                              implicit private val actionRunner: ActionRunner) {

  def byLogin(login: Login)(implicit duration: Duration): Option[SecurityUser] = {
    val eventualSecurityUser = securityUserProvider.byLogin(login)
    Await.result(eventualSecurityUser, duration)
  }

}
