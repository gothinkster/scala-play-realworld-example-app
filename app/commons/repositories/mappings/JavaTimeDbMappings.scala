package commons.repositories.mappings

import java.sql.Timestamp
import java.time.Instant

import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

trait JavaTimeDbMappings {

  implicit val localDateTimeMapper: BaseColumnType[Instant] = MappedColumnType.base[Instant, Timestamp](
    localDateTime =>
      if (localDateTime == null) null
      else Timestamp.from(localDateTime),
    timestamp =>
      if (timestamp == null) null
      else timestamp.toInstant
  )

}
