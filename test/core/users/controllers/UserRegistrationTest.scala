package core.users.controllers

import commons.validations.constraints.MinLengthViolation
import core.authentication.api.PlainTextPassword
import core.commons.models.ValidationResultWrapper
import core.users.models.UserRegistrationWrapper
import core.users.test_helpers.{SecurityUserTestHelper, UserRegistrationTestHelper, UserRegistrations, UserTestHelper}
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class UserRegistrationTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "users"

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  def securityUserTestHelper(implicit testComponents: AppWithTestComponents): SecurityUserTestHelper =
    testComponents.securityUserTestHelper

  def userTestHelper(implicit testComponents: AppWithTestComponents): UserTestHelper =
    testComponents.userTestHelper

  "user registration" should {

    "success when registration data is valid" in {
      // given
      val userRegistration = UserRegistrations.petycjaRegistration
      val login = userRegistration.username

      val registrationRequestBody = Json.toJson(UserRegistrationWrapper(userRegistration))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath").post(registrationRequestBody))

      // then
      response.status.mustBe(OK)

      val maybeSecurityUser = securityUserTestHelper.byLogin(login)
      maybeSecurityUser.isDefined.mustBe(true)

      val maybeUser = userTestHelper.byLogin(login)
      maybeUser.isDefined.mustBe(true)
    }

    "fail because given password was too short" in {
      // given
      val tooShortPassword = "short"
      val userRegistration = UserRegistrations.petycjaRegistration.copy(password = PlainTextPassword(tooShortPassword))

      val registrationRequestBody = Json.toJson(UserRegistrationWrapper(userRegistration))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath").post(Json.toJson(registrationRequestBody)))

      // then
      response.status.mustBe(UNPROCESSABLE_ENTITY)
      val validationResultWrapper = response.json.as[ValidationResultWrapper]
      validationResultWrapper.errors.size.mustBe(1)
      validationResultWrapper.errors("password").head.mustBe(MinLengthViolation(8).message)
    }
  }
}
