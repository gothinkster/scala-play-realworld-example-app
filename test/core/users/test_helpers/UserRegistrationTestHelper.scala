package core.users.test_helpers

import commons.models.Email
import commons.services.ActionRunner
import core.authentication.api.PlainTextPassword
import core.users.models.{User, UserDetailsWithToken, UserDetailsWithTokenWrapper, UserRegistration}
import core.users.services.UserRegistrationService
import org.scalatestplus.play.PortNumber
import play.api.libs.json.{JsObject, Json}
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
              (implicit portNumber: PortNumber, wsClient: WSClient): UserDetailsWithToken = {

    val requestBody = getEmailAndPasswordRequestBody(email, password)
    val response = Await.result(wsUrl(authenticatePath)
      .post(requestBody), duration)

    response.json.as[UserDetailsWithTokenWrapper].user
  }

  private def getEmailAndPasswordRequestBody(email: Email, password: PlainTextPassword) = {
    val rawEmail = email.value
    val rawPassword = password.value
    val userJsonObj = JsObject(Map("email" -> Json.toJson(rawEmail), "password" -> Json.toJson(rawPassword)))
    JsObject(Map("user" -> userJsonObj))
  }

  private def wsUrl(url: String)(implicit portNumber: PortNumber, wsClient: WSClient): WSRequest = doCall(url, wsClient, portNumber)

  private def doCall(url: String, wsClient: WSClient, portNumber: PortNumber) = {
    wsClient.url("http://localhost:" + portNumber.value + url)
  }
}
