package core.users.test_helpers

import core.users.models.{User, UserId}
import core.users.test_helpers.UserRegistrations._

object Users {
  val petycja: User = {
    User(UserId(-1), petycjaRegistration.username, petycjaRegistration.email, null, null, null, null)
  }

  val kopernik: User = {
    User(UserId(-1), kopernikRegistration.username, kopernikRegistration.email, null, null, null, null)
  }
}