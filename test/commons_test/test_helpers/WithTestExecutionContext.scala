package commons_test.test_helpers

import scala.concurrent.ExecutionContext

trait WithTestExecutionContext {
  implicit def executionContext: ExecutionContext
}
