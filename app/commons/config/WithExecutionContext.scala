package commons.config

import scala.concurrent.ExecutionContext

trait WithExecutionContext {
  implicit def executionContext: ExecutionContext
}