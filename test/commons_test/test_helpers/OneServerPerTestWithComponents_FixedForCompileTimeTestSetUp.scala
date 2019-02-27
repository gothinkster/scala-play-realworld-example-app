package commons_test.test_helpers

import org.scalatest.TestSuite
import org.scalatestplus.play.FakeApplicationFactory
import play.api.Application

/**
  * Copy of OneServerPerTestWithComponents from scalatest plus for playframework. Mixins fixed classes. Fixes were needed
  * to set up compile time tests setup.
  **/
trait OneServerPerTestWithComponents_FixedForCompileTimeTestSetUp
    extends BaseOneServerPerTest_WithBeforeAndAfterHooks
    with WithApplicationComponents_FixedForCompileTimeTestSetUp
    with FakeApplicationFactory {
  this: TestSuite =>

  override def fakeApplication(): Application = newApplication
}
