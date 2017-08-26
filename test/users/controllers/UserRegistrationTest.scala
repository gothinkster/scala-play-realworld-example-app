package users.controllers

import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.JsonBodyWritables._

import authentication.models.api.{NewSecurityUser, PlainTextPassword}
import authentication.services.api.SecurityUserProvider
import commons.models.Login
import commons.validations.constraints.MinLengthConstraint
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest
import users.controllers.mappings.UserRegistrationJsonMappings._
import users.models.UserRegistration
import users.services.api.UserProvider

class UserRegistrationTest extends RealWorldWithServerBaseTest {

  val apiPath: String = "users"

  val username: String = "alibaba"
  val password: String = "abra kadabra"
  val accessTokenJsonAttrName: String = "access_token"

  val securityUserToRegister = NewSecurityUser(Login(username), PlainTextPassword(password))

  "user registration" should {

    "success when registration data is valid" in {
      implicit val c = components
      import c._

      // given
      val userRegistration = UserRegistration(Login(username), PlainTextPassword(password))

      val userRegistrationJson = Json.toJson(userRegistration)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath/register").post(userRegistrationJson))

      // then
      response.status.mustBe(OK)
      response.body.must(include(username))
      response.body.mustNot(include(password))

      val maybeSecurityUser = runAndAwaitResult(securityUserProvider.byLogin(Login(username)))
      maybeSecurityUser.isDefined.mustBe(true)
      response.body.mustNot(include(maybeSecurityUser.get.password.value))

      val maybeUser = runAndAwaitResult(userProvider.byLogin(Login(username)))
      maybeUser.isDefined.mustBe(true)
    }

    "fail because given password was too short" in {
      // given
      implicit val c = components
      import c._

      val tooShortPassword = "short pass"
      val userRegistration = UserRegistration(Login(username), PlainTextPassword(tooShortPassword))

      val userRegistrationJson = Json.toJson(userRegistration)

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath/register").post(userRegistrationJson))

      // then
      response.status.mustBe(BAD_REQUEST)
      response.body.must(include(classOf[MinLengthConstraint].getSimpleName))
    }
  }
}



