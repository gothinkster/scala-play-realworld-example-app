package commons.repositories.mappings

import commons.models.Login
import commons.repositories.BaseRepo

import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, TableQuery => _, Rep => _, _}
import slick.lifted._

trait LoginDbMappings {

  implicit val loginMapping: BaseColumnType[Login] = MappedColumnType.base[Login, String](
    login => login.value,
    str => Login(str)
  )

}