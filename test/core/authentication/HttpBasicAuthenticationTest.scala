package core.authentication

import java.nio.charset.StandardCharsets
import java.util.Base64

import authentication.models.BearerTokenResponse
import commons.models.MissingOrInvalidCredentialsCode
import core.authentication.api.{NewSecurityUser, PlainTextPassword}
import core.commons.models.HttpExceptionResponse
import play.api.http.HeaderNames
import testhelpers.RealWorldWithServerBaseTest
import core.users.test_helpers.{UserRegistrationTestHelper, UserRegistrations}

class HttpBasicAuthenticationTest extends RealWorldWithServerBaseTest {

  val authenticatePath: String = "/authenticate"

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  def basicAuthEncode(newSecurityUser: NewSecurityUser): String = {
    val login = newSecurityUser.login.value
    val password = newSecurityUser.password.value
    val rawString = s"$login:$password"

    Base64.getEncoder.encodeToString(rawString.getBytes(StandardCharsets.UTF_8))
  }

  "Http basic authenticate" should {

    "allow valid user and password" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)

      val loginAndPasswordEncoded64 = basicAuthEncode(NewSecurityUser(registration.username, registration.password))

      // when
      val response = await(wsUrl(authenticatePath)
        .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Basic $loginAndPasswordEncoded64")
        .get())

      // then
      response.status.mustBe(OK)
      response.json.as[BearerTokenResponse].token.mustNot(equal(""))
    }

    "block not existing user" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      val loginAndPasswordEncoded64 = basicAuthEncode(NewSecurityUser(registration.username, registration.password))

      // when
      val response = await(wsUrl(authenticatePath)
        .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Basic $loginAndPasswordEncoded64")
        .get())

      // then
      response.status.mustBe(FORBIDDEN)
      response.json.as[HttpExceptionResponse].code.mustBe(MissingOrInvalidCredentialsCode)
    }

    "block user with invalid password" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      userRegistrationTestHelper.register(registration)

      val loginAndPasswordEncoded64 = basicAuthEncode(NewSecurityUser(registration.username,
        PlainTextPassword("invalid pass")))

      // when
      val response = await(wsUrl(authenticatePath)
        .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Basic $loginAndPasswordEncoded64")
        .get())

      // then
      response.status.mustBe(FORBIDDEN)
      response.json.as[HttpExceptionResponse].code.mustBe(MissingOrInvalidCredentialsCode)
    }
  }
}