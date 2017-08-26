package commons.repositories

import javax.inject.Inject

import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcBackend

import scala.concurrent.Future

class DbConfigHelper(dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  val db: JdbcBackend#DatabaseDef = dbConfig.db

  val driver: JdbcProfile = dbConfig.profile
}
