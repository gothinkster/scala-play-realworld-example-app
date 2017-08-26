package testhelpers

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

trait DefaultExecutionContext {
  protected implicit val ec: ExecutionContextExecutor = ExecutionContext.global
}
