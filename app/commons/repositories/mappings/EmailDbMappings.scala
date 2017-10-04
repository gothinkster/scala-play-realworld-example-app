package commons.repositories.mappings

import commons.models.Email
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

trait EmailDbMappings {

  implicit val emailMapping: BaseColumnType[Email] = MappedColumnType.base[Email, String](
    email => email.value,
    str => Email(str)
  )

}