package authentication.repositories.mappings

import core.authentication.api.{PasswordHash, SecurityUserId}
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

private[authentication] trait SecurityUserDbMappings {
  // todo
//  implicit val idMapping: BaseColumnType[SecurityUserId] = MappedColumnType.base[SecurityUserId, Long](
//    vo => vo.value,
//    value => SecurityUserId(value)
//  )

  implicit val passwordMapping: BaseColumnType[PasswordHash] = MappedColumnType.base[PasswordHash, String](
    password => password.value,
    str => PasswordHash(str)
  )
}
