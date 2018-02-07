package commons.services

import commons.repositories.DbConfigHelper

import scala.concurrent.Future

class ActionRunner(dbConfigHelper: DbConfigHelper) {

  import dbConfigHelper.driver.api._

  def run[A](action: DBIO[A]): Future[A] = dbConfigHelper.db.run(action)

  def runTransactionally[A](action: DBIO[A]): Future[A] = run(action.transactionally)
}
