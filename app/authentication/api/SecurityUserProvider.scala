package authentication.api

import authentication.models.SecurityUser
import commons.models.Email
import slick.dbio.DBIO

trait SecurityUserProvider {

  def findByEmailOption(email: Email): DBIO[Option[SecurityUser]]

  def findByEmail(email: Email): DBIO[SecurityUser]

}