package users.test_helpers

import authentication.models.PlainTextPassword
import commons.models.Email
import commons_test.test_helpers.{ResponseTransformer, WsScalaTestClientWithHost}
import commons_test.test_helpers.WsScalaTestClientWithHost.TestWsClient
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.libs.ws.WSResponse
import users.models._

import scala.concurrent.{ExecutionContext, Future}

class UserTestHelper(executionContext: ExecutionContext) extends WsScalaTestClientWithHost {

  def update(updateUser: UserUpdate, token: String)(implicit testWsClient: TestWsClient): Future[WSResponse] = {
    val registrationRequestBody = Json.toJson(UpdateUserWrapper(updateUser))

    wsUrl(s"/user")
      .addHttpHeaders(HeaderNames.AUTHORIZATION -> s"Token $token")
      .put(registrationRequestBody)
  }

  def login(email: Email, password: PlainTextPassword)(implicit testWsClient: TestWsClient): Future[WSResponse] = {
    val requestBody: JsValue = buildEmailAndPasswordRequestBody(email, password)

    wsUrl("/users/login")
      .post(requestBody)
  }

  private def buildEmailAndPasswordRequestBody(email: Email, password: PlainTextPassword) = {
    val rawEmail = email.value
    val rawPassword = password.value
    val userJsonObj = JsObject(Map("email" -> JsString(rawEmail), "password" -> JsString(rawPassword)))
    JsObject(Map("user" -> userJsonObj))
  }


  implicit private val ex: ExecutionContext = executionContext

  def register[ReturnType](userRegistration: UserRegistration)
              (implicit testWsClient: TestWsClient,
               responseTransformer: ResponseTransformer[ReturnType]): Future[ReturnType] = {
    require(userRegistration != null)

    wsUrl("/users")
      .post(Json.toJson(UserRegistrationWrapper(userRegistration)))
      .map(responseTransformer(_))
  }

}
