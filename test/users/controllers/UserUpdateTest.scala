package users.controllers

import authentication.models.PlainTextPassword
import commons.models.{Email, Username}
import commons_test.test_helpers.{RealWorldWithServerBaseTest, WithUserTestHelper}
import users.models.{UserDetailsWithToken, UserDetailsWithTokenWrapper, UserUpdate}
import users.test_helpers.UserRegistrations

class UserUpdateTest extends RealWorldWithServerBaseTest with WithUserTestHelper {

  "User update" should "return updated user" in await {
    val registration = UserRegistrations.petycjaRegistration
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](registration)
      updateUser = UserUpdate(Some(Email("test@test.pl")), Some(Username("test")), None, None,
        Some(PlainTextPassword("new password")))

      response <- userTestHelper.update(updateUser, userDetailsWithToken.token)
    } yield {
      response.status.mustBe(OK)
      val user = response.json.as[UserDetailsWithTokenWrapper].user
      user.email.mustBe(Email("test@test.pl"))
    }
  }

}
