package commons.repositories

import play.api.db.slick.DatabaseConfigProvider
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcBackend, JdbcProfile}

class DbConfigHelper(dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  val db: JdbcBackend#DatabaseDef = dbConfig.db

  val driver: JdbcProfile = dbConfig.profile
}
