package core.users.controllers

import commons.models.Login
import commons.validations.constraints.MinLengthConstraint
import core.authentication.api.{NewSecurityUser, PlainTextPassword}
import core.users.models.UserRegistration
import core.users.test_helpers.{SecurityUserTestHelper, UserRegistrationTestHelper, UserTestHelper}
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

  val username: String = "alibaba"
  val login = Login(username)
  val password: String = "abra kadabra"

  val accessTokenJsonAttrName: String = "access_token"
  val securityUserToRegister = NewSecurityUser(login, PlainTextPassword(password))

  "user registration" should {

    "success when registration data is valid" in {
      // given
      val userRegistration = UserRegistration(login, PlainTextPassword(password))

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
      val userRegistration = UserRegistration(login, PlainTextPassword(tooShortPassword))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath").post(Json.toJson(userRegistration)))

      // then
      response.status.mustBe(BAD_REQUEST)
      response.body.must(include(classOf[MinLengthConstraint].getSimpleName))
    }
  }
}
