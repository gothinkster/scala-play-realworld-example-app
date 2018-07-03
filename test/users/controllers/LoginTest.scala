package users.controllers

import authentication.models.PlainTextPassword
import commons.models.Email
import users.models.UserDetailsWithTokenWrapper
import users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}
import play.api.libs.json.{JsObject, JsValue, Json}
import testhelpers.RealWorldWithServerBaseTest

class LoginTest extends RealWorldWithServerBaseTest {

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  "Login" should "allow valid user and password" in {
    // given
    val registration = UserRegistrations.petycjaRegistration
    userRegistrationTestHelper.register(registration)

    val requestBody: JsValue = getEmailAndPasswordRequestBody(registration.email, registration.password)

    // when
    val response = await(wsUrl("/users/login")
      .post(requestBody))

    // then
    response.status.mustBe(OK)
    response.json.as[UserDetailsWithTokenWrapper].user.token.mustNot(equal(""))
  }

  it should "block not existing user" in {
    // given
    val registration = UserRegistrations.petycjaRegistration

    val requestBody: JsValue = getEmailAndPasswordRequestBody(registration.email, registration.password)

    // when
    val response = await(wsUrl("/users/login")
      .post(requestBody))

    // then
    response.status.mustBe(UNPROCESSABLE_ENTITY)
  }

  it should "block user with invalid password" in {
    // given
    val registration = UserRegistrations.petycjaRegistration
    userRegistrationTestHelper.register(registration)

    val requestBody: JsValue = getEmailAndPasswordRequestBody(registration.email, PlainTextPassword("wrong pass"))

    // when
    val response = await(wsUrl("/users/login")
      .post(requestBody))

    // then
    response.status.mustBe(UNPROCESSABLE_ENTITY)
  }

  private def getEmailAndPasswordRequestBody(email: Email, password: PlainTextPassword) = {
    val rawEmail = email.value
    val rawPassword = password.value
    val userJsonObj = JsObject(Map("email" -> Json.toJson(rawEmail), "password" -> Json.toJson(rawPassword)))
    JsObject(Map("user" -> userJsonObj))
  }
}