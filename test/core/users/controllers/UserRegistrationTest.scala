package core.users.controllers

import commons.validations.constraints.MinLengthConstraint
import core.authentication.api.PlainTextPassword
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

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath").post(Json.toJson(userRegistration)))

      // then
      response.status.mustBe(OK)

      val maybeSecurityUser = securityUserTestHelper.byLogin(login)
      maybeSecurityUser.isDefined.mustBe(true)

      val maybeUser = userTestHelper.byLogin(login)
      maybeUser.isDefined.mustBe(true)
    }

    "fail because given password was too short" in {
      // given
      val tooShortPassword = "short pass"
      val userRegistration = UserRegistrations.petycjaRegistration.copy(password = PlainTextPassword(tooShortPassword))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath").post(Json.toJson(userRegistration)))

      // then
      response.status.mustBe(BAD_REQUEST)
      response.body.must(include(classOf[MinLengthConstraint].getSimpleName))
    }
  }
}
