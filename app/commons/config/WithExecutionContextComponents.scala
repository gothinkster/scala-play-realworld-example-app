package commons.config

import scala.concurrent.ExecutionContext

trait WithExecutionContextComponents {
  implicit def executionContext: ExecutionContext
}