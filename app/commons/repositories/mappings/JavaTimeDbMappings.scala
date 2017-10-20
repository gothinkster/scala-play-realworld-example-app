package commons.repositories.mappings

import java.sql.Timestamp
import java.time.LocalDateTime

import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

trait JavaTimeDbMappings {

  implicit val localDateTimeMapper: BaseColumnType[LocalDateTime] = MappedColumnType.base[LocalDateTime, Timestamp](
    localDateTime =>
      if (localDateTime == null) null
      else Timestamp.valueOf(localDateTime),
    timestamp =>
      if (timestamp == null) null
      else timestamp.toLocalDateTime
  )

}
