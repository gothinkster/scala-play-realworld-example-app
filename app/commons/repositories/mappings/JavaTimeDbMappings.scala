package commons.repositories.mappings

import java.sql.Timestamp
import java.time.LocalDateTime

import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, TableQuery => _, Rep => _, _}
import slick.lifted._

trait JavaTimeDbMappings {

  implicit val localDateTimeMapper: BaseColumnType[LocalDateTime] = MappedColumnType.base[LocalDateTime, Timestamp](
    Timestamp.valueOf, _.toLocalDateTime
  )

}
