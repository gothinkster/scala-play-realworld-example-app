package users.test_helpers

import commons.models.{Email, Username}
import authentication.api.PlainTextPassword
import users.models.UserRegistration

object UserRegistrations {
  val petycjaRegistration: UserRegistration =
    UserRegistration(Username("petycja"), PlainTextPassword("a valid password"), Email("petycja@buziaczek.pl"))

  val kopernikRegistration: UserRegistration =
    UserRegistration(Username("kopernik"), PlainTextPassword("a valid password"), Email("kopernik@torun.pl"))
}