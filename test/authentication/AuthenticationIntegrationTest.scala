//package authentication
//
//import java.time.LocalDateTime
//
//import authentication.models.api.{NewSecurityUser, PlainTextPassword}
//import authentication.oauth2.AuthenticationConfig
//import be.objectify.deadbolt.scala.ActionBuilders
//import com.softwaremill.macwire.wire
//import commons.models.Login
//import commons.utils.DbioUtils
//import config.ExampleComponents
//import play.api.libs.ws.{WSClient, WSResponse}
//import play.api.mvc._
//import play.api.routing.Router
//import play.api.routing.sird._
//import testhelpers.PlayWithFoodWithServerBaseTest
//
//import scala.concurrent.Future
//import scalaoauth2.provider.OAuth2Provider
//
//class AuthenticationIntegrationTest extends PlayWithFoodWithServerBaseTest {
//
//  val fakeApiPath: String = "test"
//
//  val username: String = "alibaba"
//  val password: String = "password"
//  val accessTokenJsonAttrName: String = "access_token"
//
//  val accessTokenPath: String = "/oauth/accessToken"
//
//  val securityUserToRegister = NewSecurityUser(Login(username), PlainTextPassword(password))
//
//  def authenticate(newSecurityUser: NewSecurityUser, authenticationConfig: AuthenticationConfig)
//                  (implicit wsClient: WSClient): String = {
//    val eventualToken: Future[String] = wsUrl(accessTokenPath)
//      .post(Map(
//        "grant_type" -> Seq("implicit"),
//        "client_id" -> Seq(authenticationConfig.clientId),
//        "username" -> Seq(newSecurityUser.login.value),
//        "password" -> Seq(newSecurityUser.password.value)
//      ))
//      .map(_.json)
//      .map(json => json \ "access_token")
//      .map(_.as[String])
//
//    await(eventualToken)
//  }
//
//  "authentication" should {
//
//    "allow everyone to public API" in {
//      val c = components
//      import c._
//
//      // when
//      val response: WSResponse = await(wsUrl(s"/$fakeApiPath/public").get())
//
//      // then
//      response.status.mustBe(OK)
//    }
//
//    "block unauthenticated request" in {
//      val c = components
//      import c._
//      // when
//
//      val response: WSResponse = await(wsUrl(s"/$fakeApiPath/testScopeRequired").get())
//
//      // then
//      response.status.mustBe(UNAUTHORIZED)
//    }
//
//    "allow user with test scope" in {
//      val c = components
//      import c._
//
//      // given
//      runAndAwaitResult(securityUserCreator.create(securityUserToRegister))(c)
//
//      val accessToken = authenticate(securityUserToRegister, authenticationConfig)(wsClient)
//
//      // when
//      val response: WSResponse = await(wsUrl(s"/$fakeApiPath/testScopeRequired")
//        .addHttpHeaders("Authorization" -> s"Bearer $accessToken")
//        .get())
//
//      // then
//      response.status.mustBe(OK)
//    }
//
//    "reuse already issued and still valid token" in {
//      val c = components
//      import c._
//
//      // given
//      runAndAwaitResult(securityUserCreator.create(securityUserToRegister))(c)
//
//      // when
//      val accessToken = authenticate(securityUserToRegister, authenticationConfig)
//      val accessToken2 = authenticate(securityUserToRegister, authenticationConfig)
//
//      // then
//      accessToken.mustEqual(accessToken2)
//    }
//
//    "not allow expired token" in {
//      val c = components
//      import c._
//
//      // given
//      runAndAwaitResult(securityUserCreator.create(securityUserToRegister))(c)
//
//      val accessToken = authenticate(securityUserToRegister, authenticationConfig)
//      expireAccessToken(accessToken, c)
//
//      // when
//      val response: WSResponse = await(wsUrl(s"/$fakeApiPath/testScopeRequired")
//        .addHttpHeaders("Authorization" -> s"Bearer $accessToken")
//        .get())
//
//      // then
//      response.status.mustBe(UNAUTHORIZED)
//    }
//  }
//
//  private def expireAccessToken(accessToken: String, components: ExampleComponents) = {
//    val expiredTokenDateTime = LocalDateTime.of(2016, 1, 1, 1, 1, 1)
//    val updateCreatedDateTime = components.accessTokenRepo.byToken(accessToken)
//      .flatMap(DbioUtils.optionToDbio(_))
//      .map(_.copy(createdAt = expiredTokenDateTime))
//      .flatMap(accessToken => components.accessTokenRepo.update(accessToken))
//
//    runAndAwaitResult(updateCreatedDateTime)(components)
//  }
//
//  override def components: PlayWithFoodWithTestConfig = new PlayWithFoodWithTestConfig {
//
//    lazy val authenticationTestController: AuthenticationTestController = wire[AuthenticationTestController]
//
//    override lazy val router: Router = {
//      Router.from {
//        case POST(p"/oauth/accessToken") => oAuth2Controller.accessToken
//
//        case GET(p"/test/public") => authenticationTestController.public
//
//        case GET(p"/test/testScopeRequired") => authenticationTestController.testScope
//      }
//    }
//  }
//
//  class AuthenticationTestController(actionBuilders: ActionBuilders, components: ControllerComponents)
//    extends AbstractController(components) with OAuth2Provider {
//
//    def public: Action[AnyContent] = Action { _ =>
//      Results.Ok
//    }
//
//    def testScope: Action[AnyContent] = {
//      actionBuilders.RestrictAction("play-with-food")(components.parsers).defaultHandler() { implicit request =>
//        Future(Ok)
//      }
//    }
//  }
//}