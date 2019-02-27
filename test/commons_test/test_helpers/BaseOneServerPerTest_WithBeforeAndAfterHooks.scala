/*
 * Copyright 2001-2016 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package commons_test.test_helpers

import org.scalatest._
import org.scalatestplus.play.{FakeApplicationFactory, ServerProvider}
import play.api.Application
import play.api.test._

/**
  * Copy of BaseOneServerPerTest from scalatest plus for playframework. Two changes were made: first change adds
  * "before" and "after hooks. The may be required by testes to handle context with server running.
  * Second change disables synchronization. It uses whole instance of this class as a lock, deadlock is possible, when
  * mixins to test suit contains lazy vals. Tests in this example projects makes are not parallel anyway,
  * so synchronization is not required.
  */
trait BaseOneServerPerTest_WithBeforeAndAfterHooks extends TestSuiteMixin with ServerProvider {
  this: TestSuite with FakeApplicationFactory =>

  @volatile private var privateApp: Application = _
  @volatile private var privateServer: RunningServer = _

  implicit final def app: Application = {
    val a = privateApp
    if (a == null) {
      throw new IllegalStateException("Test isn't running yet so application is not available")
    }
    a
  }

  implicit final def runningServer: RunningServer = {
    val rs = privateServer
    if (rs == null) {
      throw new IllegalStateException("Test isn't running yet so the server endpoints are not available")
    }
    privateServer
  }

  abstract override def withFixture(test: NoArgTest): Outcome = {
    // Need to synchronize within a suite because we store current app/server in fields in the class
    // Could possibly pass app/server info in a ScalaTest object?
//    synchronized { // synchronization is disabled. Only sequential tests are supported.
      privateApp = newAppForTest(test)
      privateServer = newServerForTest(app, test)
      afterServerStarted()
      try super.withFixture(test) finally {
        try {
          beforeServerStopped()
        } catch {
          case e: Throwable =>
            System.err.println(s"Test could not be cleaned up: $e")
          // continue to release resources
        }
        val rs = privateServer // Store before nulling fields
        privateApp = null
        privateServer = null
        // Stop server and release locks
        rs.stopServer.close()
//      }
    }
  }


  def newAppForTest(testData: TestData): Application = fakeApplication()

  protected def newServerForTest(app: Application, testData: TestData): RunningServer =
    DefaultTestServerFactory.start(app)

  def afterServerStarted(): Unit = ()

  def beforeServerStopped(): Unit = ()

}

