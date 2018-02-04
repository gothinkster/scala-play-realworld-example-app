package commons.repositories

import javax.inject.Inject

import scala.concurrent.Future

class ActionRunner(dbConfigHelper: DbConfigHelper) {

  import dbConfigHelper.driver.api._

  def run[A](action: DBIO[A]): Future[A] = dbConfigHelper.db.run(action)

  def runInTransaction[A](action: DBIO[A]): Future[A] = run(action.transactionally)
}
