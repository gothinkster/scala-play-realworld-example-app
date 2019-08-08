package users.controllers

import authentication.models.PlainTextPassword
import commons_test.test_helpers.{RealWorldWithServerAndTestConfigBaseTest, WithArticleTestHelper, WithUserTestHelper}
import users.models.{UserDetailsWithToken, UserDetailsWithTokenWrapper}
import users.test_helpers.UserRegistrations

class LoginTest extends RealWorldWithServerAndTestConfigBaseTest with WithArticleTestHelper with WithUserTestHelper {

  "Login" should "allow valid user and password" in await {
    val registration = UserRegistrations.petycjaRegistration
    for {
      _ <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)

      response <- userTestHelper.login(registration.email, registration.password)
    } yield {
      response.status.mustBe(OK)
        response.json.as[UserDetailsWithTokenWrapper].user.token.mustNot(equal(""))
    }
  }

  it should "block not existing user" in await {
    val registration = UserRegistrations.petycjaRegistration
    for {
      response <- userTestHelper.login(registration.email, registration.password)
    } yield {
      response.status.mustBe(UNPROCESSABLE_ENTITY)
    }
  }

  it should "block user with invalid password" in await {
    val registration = UserRegistrations.petycjaRegistration
    for {
      _ <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)

      response <- userTestHelper.login(registration.email, PlainTextPassword("wrong pass"))
    } yield {
      response.status.mustBe(UNPROCESSABLE_ENTITY)
    }
  }

}