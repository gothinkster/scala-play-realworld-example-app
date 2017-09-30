package core.authentication

import core.authentication.api.{AuthenticatedActionBuilder, AuthenticatedUser}
import com.softwaremill.macwire.wire
import commons.models.MissingOrInvalidCredentialsCode
import core.commons.models.HttpExceptionResponse
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import testhelpers.RealWorldWithServerBaseTest
import core.users.config.{UserRegistrationTestHelper, UserRegistrations}

import scala.concurrent.ExecutionContext

class JwtAuthenticationTest extends RealWorldWithServerBaseTest {

  val fakeApiPath: String = "test"

  val accessTokenJsonAttrName: String = "access_token"

  def userRegistrationTestHelper(implicit testComponents: AppWithTestComponents): UserRegistrationTestHelper =
    testComponents.userRegistrationTestHelper

  "authentication" should {

    "allow everyone to public API" in {
      // when
      val response: WSResponse = await(wsUrl(s"/$fakeApiPath/public").get())

      // then
      response.status.mustBe(OK)
    }

    "block request without jwt token" in {
      // when
      val response: WSResponse = await(wsUrl(s"/$fakeApiPath/authenticationRequired").get())

      // then
      response.status.mustBe(UNAUTHORIZED)
      response.json.as[HttpExceptionResponse].code.mustBe(MissingOrInvalidCredentialsCode)
    }

    "block request with invalid jwt token" in {
      // given
      val token = "invalidJwtToken"

      // when
      val response: WSResponse = await(wsUrl(s"/$fakeApiPath/authenticationRequired")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer $token")
        .get())

      // then
      response.status.mustBe(UNAUTHORIZED)
      response.json.as[HttpExceptionResponse].code.mustBe(MissingOrInvalidCredentialsCode)
    }

    "allow authenticated user to secured API" in {
      // given
      val registration = UserRegistrations.petycjaRegistration
      val user = userRegistrationTestHelper.register(registration)
      val tokenResponse = userRegistrationTestHelper.getToken(registration.login, registration.password)

      // when
      val response: WSResponse = await(wsUrl(s"/$fakeApiPath/authenticationRequired")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Bearer ${tokenResponse.token}")
        .get())

      // then
      response.status.mustBe(OK)
      response.json.as[AuthenticatedUser].login.mustBe(user.login)
    }

    "block expired jwt token" in {
      fail("todo")
    }

  }

  override def components: RealWorldWithTestConfig = new RealWorldWithTestConfig {

    lazy val authenticationTestController: AuthenticationTestController = wire[AuthenticationTestController]

    override lazy val router: Router = {
      val testControllerRoutes: PartialFunction[RequestHeader, Handler] = {
        case GET(p"/test/public") => authenticationTestController.public
        case GET(p"/test/authenticationRequired") => authenticationTestController.authenticated
      }

      Router.from(routes.orElse(testControllerRoutes))
    }
  }

  class AuthenticationTestController(authenticatedAction: AuthenticatedActionBuilder,
                                     components: ControllerComponents,
                                     implicit private val ex: ExecutionContext)
    extends AbstractController(components) {

    def public: Action[AnyContent] = Action { _ =>
      Results.Ok
    }

    def authenticated: Action[AnyContent] = authenticatedAction { request =>
      Ok(Json.toJson(request.user))
    }

  }
}