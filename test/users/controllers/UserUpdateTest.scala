package users.controllers

import commons.models.{Email, Username}
import authentication.api.PlainTextPassword
import users.models.{UpdateUserWrapper, UserDetailsWithTokenWrapper, UserUpdate}
import users.test_helpers.{SecurityUserTestHelper, UserRegistrationTestHelper, UserRegistrations, UserTestHelper}
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import play.api.http.HeaderNames
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class UserUpdateTest extends RealWorldWithServerBaseTest {

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  def securityUserTestHelper(implicit testComponents: AppWithTestComponents): SecurityUserTestHelper =
    testComponents.securityUserTestHelper

  def userTestHelper(implicit testComponents: AppWithTestComponents): UserTestHelper =
    testComponents.userTestHelper

  def jwtAuthenticator(implicit testComponents: AppWithTestComponents): JwtAuthenticator =
    testComponents.jwtAuthenticator

  "User update" should "return new json web token" in {
    // given
    val registration = UserRegistrations.petycjaRegistration
    userRegistrationTestHelper.register(registration)
    val tokenResponse = userRegistrationTestHelper.getToken(registration.email, registration.password)

    val newEmail = Email("test@test.pl")
    val updateUser = UserUpdate(Some(newEmail), Some(Username("test")), None, None,
      Some(PlainTextPassword("new password")))
    val registrationRequestBody = Json.toJson(UpdateUserWrapper(updateUser))

    // when
    val response: WSResponse = await(wsUrl(s"/user")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${tokenResponse.token}")
      .put(registrationRequestBody))

    // then
    response.status.mustBe(OK)
    val user = response.json.as[UserDetailsWithTokenWrapper].user
    user.email.mustBe(newEmail)
    val rawToken = user.token
    jwtAuthenticator.validateToken(rawToken).mustNot(be(null))
  }

}
