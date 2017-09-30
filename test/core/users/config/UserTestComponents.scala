package core.users.config

import java.nio.charset.StandardCharsets
import java.util.Base64

import authentication.AuthenticationComponents
import authentication.models.BearerTokenResponse
import commons.models.Login
import commons.repositories.ActionRunner
import core.authentication.api.PlainTextPassword
import org.scalatestplus.play.PortNumber
import play.api.http.HeaderNames
import play.api.libs.ws.{WSClient, WSRequest}
import testhelpers.TestUtils
import core.users.UserComponents
import core.users.models.{User, UserRegistration}
import core.users.services.UserRegistrationService
import play.api.libs.json.Reads

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

trait UserTestComponents {
  _: AuthenticationComponents with UserComponents =>

  lazy val userRegistrationTestHelper: UserRegistrationTestHelper =
    new UserRegistrationTestHelper(userRegistrationService, "/authenticate", actionRunner)
}

class UserRegistrationTestHelper(userRegistrationService: UserRegistrationService,
                                 authenticatePath: String,
                                 implicit private val actionRunner: ActionRunner) {

  private val duration = new DurationInt(1).minute

  def register(userRegistration: UserRegistration): User = {
    require(userRegistration != null)

    val action = userRegistrationService.register(userRegistration)
    TestUtils.runAndAwaitResult(action)(actionRunner, duration)
  }

  def getToken(login: Login, password: PlainTextPassword)
              (implicit portNumber: PortNumber, wsClient: WSClient,
               bearerTokenResponseReads: Reads[BearerTokenResponse]): BearerTokenResponse = {
    val rawLogin = login.value
    val rawPassword = password.value
    val rawString = s"$rawLogin:$rawPassword"

    val loginAndPasswordEncoded64 = Base64.getEncoder.encodeToString(rawString.getBytes(StandardCharsets.UTF_8))

    val response = Await.result(wsUrl(authenticatePath)
      .withHttpHeaders(HeaderNames.AUTHORIZATION -> s"Basic $loginAndPasswordEncoded64")
      .get(), duration)

    response.json.as[BearerTokenResponse]
  }

  private def wsUrl(url: String)(implicit portNumber: PortNumber, wsClient: WSClient): WSRequest = doCall(url, wsClient, portNumber)

  private def doCall(url: String, wsClient: WSClient, portNumber: PortNumber) = {
    wsClient.url("http://localhost:" + portNumber.value + url)
  }
}

object UserRegistrations {
  val petycjaRegistration = UserRegistration(Login("petycja"), PlainTextPassword("a valid password"))
}