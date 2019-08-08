package commons_test.test_helpers

import play.api.{BuiltInComponents, _}

/**
  * Copy of WithApplicationComponents from scalatest plus for playframework. Changes were made to allow compile time
  * tests setup.
  **/
trait WithApplicationComponents_FixedForCompileTimeTestSetUp {

  def createComponents: TestComponents

  type TestComponents <: BuiltInComponents

  /**
    * has the same purpose as BaseOneServerPerTest.app but in compile time set up
    */
  var components: TestComponents = _

  /**
    * additionally stores components instance for each test case. Needed to access app's dependencies in compile time set up
    */
  final def newApplication: Application = {
    components = createComponents
    components.application
  }

  /**
    * changed to def from lazy val to properly clean up all resources after each test in compile time set up
    */
  def context: ApplicationLoader.Context = {
    val env = Environment.simple()
    ApplicationLoader.Context.create(env)
  }
}
