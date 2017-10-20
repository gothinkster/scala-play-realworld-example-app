package commons.repositories.mappings

import commons.models.Username
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

trait UsernameDbMappings {

  implicit val usernameMapping: BaseColumnType[Username] = MappedColumnType.base[Username, String](
    username => username.value,
    str => Username(str)
  )

}