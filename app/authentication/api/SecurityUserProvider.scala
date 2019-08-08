package authentication.api

import authentication.models.{SecurityUser, SecurityUserId}
import commons.models.Email
import slick.dbio.DBIO

trait SecurityUserProvider {

  def findById(securityUserId: SecurityUserId): DBIO[SecurityUser]

  def findByEmailOption(email: Email): DBIO[Option[SecurityUser]]

  def findByEmail(email: Email): DBIO[SecurityUser]

}