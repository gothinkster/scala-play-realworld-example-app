package users.controllers

import commons.models.Login
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import testhelpers.RealWorldWithServerBaseTest
import users.controllers.mappings.UserJsonMappings._
import users.models.{User, UserId}
import users.repositories.UserRepo

class UserControllerTest extends RealWorldWithServerBaseTest {
  val apiPath: String = "users"

  "user API" should {
    "return list with Marek" in {
      implicit val c = components
      import c._

      // given
      val user = User(UserId(1), Login("Marek"))
      val persistedUser = runAndAwaitResult(userRepo.create(user))

      // when
      val response: WSResponse = await(wsUrl(s"/$apiPath").get)

      // then
      response.status.mustBe(OK)

      val expectedBody: JsValue = Json.toJson(List(persistedUser))
      response.body.mustBe(expectedBody.toString())
    }
  }
}
