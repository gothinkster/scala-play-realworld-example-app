package authentication

import authentication.api.AuthenticatedActionBuilder
import authentication.exceptions.MissingOrInvalidCredentialsCode
import authentication.models.{AuthenticatedUser, HttpExceptionResponse}
import com.softwaremill.macwire.wire
import commons_test.test_helpers.RealWorldWithServerAndTestConfigBaseTest.RealWorldWithTestConfig
import commons_test.test_helpers.{ProgrammaticDateTimeProvider, RealWorldWithServerAndTestConfigBaseTest, WithUserTestHelper}
import play.api.ApplicationLoader.Context
import play.api.http.HeaderNames
import play.api.libs.json._
import play.api.mvc._
import play.api.routing.Router
import play.api.routing.sird._
import users.models.UserDetailsWithToken
import users.test_helpers.UserRegistrations

import scala.concurrent.ExecutionContext

class JwtAuthenticationTest extends RealWorldWithServerAndTestConfigBaseTest with WithUserTestHelper {

  val fakeApiPath: String = "test"

  val accessTokenJsonAttrName: String = "access_token"

  val programmaticDateTimeProvider = new ProgrammaticDateTimeProvider

  "Authentication" should "allow everyone to public API" in await {
    for {
      response <- wsUrl(s"/$fakeApiPath/public").get()
    } yield {
      response.status.mustBe(OK)
    }
  }

  it should "block request without jwt token" in await {
    for {
      response <- wsUrl(s"/$fakeApiPath/authenticationRequired").get()
    } yield {
      response.status.mustBe(UNAUTHORIZED)
      response.json.as[HttpExceptionResponse].code.mustBe(MissingOrInvalidCredentialsCode)
    }
  }

  it should "block request with invalid jwt token" in await {
    for {
      response <- wsUrl(s"/$fakeApiPath/authenticationRequired")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> "Token invalidJwtToken")
        .get()
    } yield {
      response.status.mustBe(UNAUTHORIZED)
      response.json.as[HttpExceptionResponse].code.mustBe(MissingOrInvalidCredentialsCode)
    }
  }

  it should "allow authenticated user to secured API" in await {
    for {
      userDetailsWithToken <- userTestHelper.register[UserDetailsWithToken](UserRegistrations.petycjaRegistration)

      response <- wsUrl(s"/$fakeApiPath/authenticationRequired")
        .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token ${userDetailsWithToken.token}")
        .get()
    } yield {
      response.status.mustBe(OK)
      response.json.as[AuthenticatedUser].email.mustBe(userDetailsWithToken.email)
    }
  }

  override def createComponents: RealWorldWithTestConfig =
    new JwtAuthenticationTestComponents(programmaticDateTimeProvider, context)
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

class JwtAuthenticationTestComponents(dateTimeProvider: ProgrammaticDateTimeProvider, context: Context)
  extends RealWorldWithTestConfig(context) {

  lazy val authenticationTestController: AuthenticationTestController = wire[AuthenticationTestController]

  override lazy val router: Router = {
    val testControllerRoutes: PartialFunction[RequestHeader, Handler] = {
      case GET(p"/test/public") => authenticationTestController.public
      case GET(p"/test/authenticationRequired") => authenticationTestController.authenticated
    }

    Router.from(routes.orElse(testControllerRoutes))
  }
}
