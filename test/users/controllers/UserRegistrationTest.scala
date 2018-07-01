package users.controllers

import commons.validations.constraints.{EmailAlreadyTakenViolation, MinLengthViolation, UsernameAlreadyTakenViolation}
import authentication.api.PlainTextPassword
import commons.models.ValidationResultWrapper
import users.models.{UserDetailsWithTokenWrapper, UserRegistrationWrapper}
import users.test_helpers.{SecurityUserTestHelper, UserRegistrationTestHelper, UserRegistrations, UserTestHelper}
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest

class UserRegistrationTest extends RealWorldWithServerBaseTest {

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  def securityUserTestHelper(implicit testComponents: AppWithTestComponents): SecurityUserTestHelper =
    testComponents.securityUserTestHelper

  def userTestHelper(implicit testComponents: AppWithTestComponents): UserTestHelper =
    testComponents.userTestHelper

  def jwtAuthenticator(implicit testComponents: AppWithTestComponents): JwtAuthenticator =
    testComponents.jwtAuthenticator

  "User registration" should "success when registration data is valid" in {
    // given
    val userRegistration = UserRegistrations.petycjaRegistration

    val registrationRequestBody = Json.toJson(UserRegistrationWrapper(userRegistration))

    // when
    val response: WSResponse = await(wsUrl("/users").post(registrationRequestBody))

    // then
    response.status.mustBe(OK)

    val maybeSecurityUser = securityUserTestHelper.byEmail(userRegistration.email)
    maybeSecurityUser.isDefined.mustBe(true)

    val maybeUser = userTestHelper.byLogin(userRegistration.username)
    maybeUser.isDefined.mustBe(true)
  }

  it should "use email as username in order to generate proper Jwt token" in {
    // given
    val userRegistration = UserRegistrations.petycjaRegistration

    val registrationRequestBody = Json.toJson(UserRegistrationWrapper(userRegistration))

    // when
    val response: WSResponse = await(wsUrl("/users").post(registrationRequestBody))

    // then
    response.status.mustBe(OK)
    val rawJwtToken = response.json.as[UserDetailsWithTokenWrapper].user.token
    jwtAuthenticator.validateToken(rawJwtToken).mustNot(be(null))
  }

  it should "fail because given password was too short" in {
    // given
    val tooShortPassword = "short"
    val userRegistration = UserRegistrations.petycjaRegistration.copy(password = PlainTextPassword(tooShortPassword))

    val registrationRequestBody = Json.toJson(UserRegistrationWrapper(userRegistration))

    // when
    val response: WSResponse = await(wsUrl("/users").post(Json.toJson(registrationRequestBody)))

    // then
    response.status.mustBe(UNPROCESSABLE_ENTITY)
    val validationResultWrapper = response.json.as[ValidationResultWrapper]
    validationResultWrapper.errors.size.mustBe(1)
    validationResultWrapper.errors("password").head.mustBe(MinLengthViolation(8).message)
  }

  it should "fail because login has already been taken" in {
    // given
    val userRegistration = UserRegistrations.petycjaRegistration

    userRegistrationTestHelper.register(userRegistration)

    val registrationRequestBody = Json.toJson(UserRegistrationWrapper(userRegistration))

    // when
    val response: WSResponse = await(wsUrl("/users").post(Json.toJson(registrationRequestBody)))

    // then
    response.status.mustBe(UNPROCESSABLE_ENTITY)
    val validationResultWrapper = response.json.as[ValidationResultWrapper]
    validationResultWrapper.errors.size.mustBe(>=(1))
    validationResultWrapper.errors("username").head.mustBe(UsernameAlreadyTakenViolation(userRegistration.username).message)
  }

  it should "fail because email has already been taken" in {
    // given
    val userRegistration = UserRegistrations.petycjaRegistration

    userRegistrationTestHelper.register(userRegistration)

    val registrationRequestBody = Json.toJson(UserRegistrationWrapper(userRegistration))

    // when
    val response: WSResponse = await(wsUrl("/users").post(Json.toJson(registrationRequestBody)))

    // then
    response.status.mustBe(UNPROCESSABLE_ENTITY)
    val validationResultWrapper = response.json.as[ValidationResultWrapper]
    validationResultWrapper.errors.size.mustBe(>=(1))
    validationResultWrapper.errors("email").head.mustBe(EmailAlreadyTakenViolation(userRegistration.email).message)
  }

}
