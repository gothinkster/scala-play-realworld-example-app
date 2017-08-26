package commons.repositories

import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _}
import slick.lifted._

trait BaseId[U] extends Any with MappedTo[U] {

  def id: Underlying

  override def value: Underlying = id

}
