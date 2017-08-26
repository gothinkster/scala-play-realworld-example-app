package authentication.repositories

import authentication.models.PasswordHash
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

trait SecurityUserDbMappings {

  implicit val passwordMapping: BaseColumnType[PasswordHash] = MappedColumnType.base[PasswordHash, String](
    password => password.value,
    str => PasswordHash(str)
  )

}
