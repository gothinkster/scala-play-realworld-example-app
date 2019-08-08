package users.controllers

import authentication.models.PlainTextPassword
import commons.models.ValidationResultWrapper
import commons.validations.constraints.{EmailAlreadyTakenViolation, MinLengthViolation, UsernameAlreadyTakenViolation}
import commons_test.test_helpers.RealWorldWithServerAndTestConfigBaseTest
import play.api.libs.ws.WSResponse
import users.test_helpers.{UserRegistrations, UserTestHelper}

class UserRegistrationTest extends RealWorldWithServerAndTestConfigBaseTest {

  def userTestHelper: UserTestHelper = new UserTestHelper(executionContext)

  "User registration" should "success when registration data is valid" in await {
    for {
      response <- userTestHelper.register[WSResponse](UserRegistrations.petycjaRegistration)
    } yield {
      response.status.mustBe(OK)
    }
  }

  it should "fail because given password was too short" in await {
    val userRegistration = UserRegistrations.petycjaRegistration.copy(password = PlainTextPassword("short"))
    for {
      response <- userTestHelper.register[WSResponse](userRegistration)
    } yield {
      response.status.mustBe(UNPROCESSABLE_ENTITY)
      val validationResultWrapper = response.json.as[ValidationResultWrapper]
      validationResultWrapper.errors.size.mustBe(1)
      validationResultWrapper.errors("password").head.mustBe(MinLengthViolation(8).message)
    }
  }

  it should "fail because login has already been taken" in await {
    val userRegistration = UserRegistrations.petycjaRegistration
    for {
      _ <- userTestHelper.register[WSResponse](userRegistration)
      response <- userTestHelper.register[WSResponse](userRegistration)
    } yield {
      response.status.mustBe(UNPROCESSABLE_ENTITY)
      val validationResultWrapper = response.json.as[ValidationResultWrapper]
      validationResultWrapper.errors.size.mustBe(>=(1))
      validationResultWrapper.errors("username").head.mustBe(UsernameAlreadyTakenViolation(userRegistration.username).message)
    }
  }

  it should "fail because email has already been taken" in await {
    val userRegistration = UserRegistrations.petycjaRegistration
    for {
      _ <- userTestHelper.register[WSResponse](userRegistration)
      response <- userTestHelper.register[WSResponse](userRegistration)
    } yield {
      response.status.mustBe(UNPROCESSABLE_ENTITY)
      val validationResultWrapper = response.json.as[ValidationResultWrapper]
      validationResultWrapper.errors.size.mustBe(>=(1))
      validationResultWrapper.errors("email").head.mustBe(EmailAlreadyTakenViolation(userRegistration.email).message)
    }
  }

}
