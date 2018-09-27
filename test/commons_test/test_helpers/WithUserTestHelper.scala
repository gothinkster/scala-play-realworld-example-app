package commons_test.test_helpers

import users.models.{UserDetailsWithToken, UserDetailsWithTokenWrapper}
import users.test_helpers.{ProfileTestHelper, UserTestHelper}

trait WithUserTestHelper {
  _: WithTestExecutionContext =>

  implicit val userDetailsWithTokenWrapperResponseTransformer: ResponseTransformer[UserDetailsWithToken] = {
    FromJsonToModelResponseTransformerFactory[UserDetailsWithTokenWrapper, UserDetailsWithToken](_.user)
  }

  def userTestHelper: UserTestHelper = new UserTestHelper(executionContext)

  def profileTestHelper: ProfileTestHelper = new ProfileTestHelper(executionContext)
}
