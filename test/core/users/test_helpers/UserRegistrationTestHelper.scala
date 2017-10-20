package core.users.test_helpers

import java.nio.charset.StandardCharsets
import java.util.Base64

import authentication.models.BearerTokenResponse
import commons.models.Email
import commons.repositories.ActionRunner
import core.authentication.api.PlainTextPassword
import core.users.models.{User, UserRegistration}
import core.users.services.UserRegistrationService
import org.scalatestplus.play.PortNumber
import play.api.http.HeaderNames
import play.api.libs.json.Reads
import play.api.libs.ws.{WSClient, WSRequest}
import testhelpers.TestUtils

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class UserRegistrationTestHelper(userRegistrationService: UserRegistrationService,
                                 authenticatePath: String,
                                 implicit private val actionRunner: ActionRunner) {

  private val duration = new DurationInt(1).minute

  def register(userRegistration: UserRegistration): User = {
    require(userRegistration != null)

    val action = userRegistrationService.register(userRegistration)
    TestUtils.runAndAwaitResult(action)(actionRunner, duration)
  }

  def getToken(email: Email, password: PlainTextPassword)
              (implicit portNumber: PortNumber, wsClient: WSClient,
               bearerTokenResponseReads: Reads[BearerTokenResponse]): BearerTokenResponse = {
    val rawEmail = email.value
    val rawPassword = password.value
    val rawString = s"$rawEmail:$rawPassword"

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
