package authentication.repositories.mappings

import core.authentication.api.PasswordHash
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

private[authentication] trait SecurityUserDbMappings {

  implicit val passwordMapping: BaseColumnType[PasswordHash] = MappedColumnType.base[PasswordHash, String](
    password => password.value,
    str => PasswordHash(str)
  )

}
