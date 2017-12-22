package core.users.test_helpers

import core.users.models.{User, UserId}
import core.users.test_helpers.UserRegistrations._

object Users {
  val petycja: User = {
    User(UserId(-1), petycjaRegistration.username, petycjaRegistration.email, null, null)
  }
}