package authentication.repositories.mappings

import authentication.models.SecurityUserId
import commons.repositories.BaseRepo

import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, TableQuery => _, Rep => _, _}
import slick.lifted._

private[authentication] trait SecurityUserDbMappings {

  implicit val idMapping: BaseColumnType[SecurityUserId] = MappedColumnType.base[SecurityUserId, Long](
    vo => vo.value,
    value => SecurityUserId(value)
  )

}