package core.users.test_helpers

import commons.models.{Email, Username}
import core.authentication.api.PlainTextPassword
import core.users.models.UserRegistration

object UserRegistrations {
  val petycjaRegistration: UserRegistration =
    UserRegistration(Username("petycja"), PlainTextPassword("a valid password"), Email("petycja@buziaczek.pl"))

  val kopernikRegistration: UserRegistration =
    UserRegistration(Username("kopernik"), PlainTextPassword("a valid password"), Email("kopernik@torun.pl"))
}