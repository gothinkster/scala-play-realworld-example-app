package authentication.api

import commons.models.Email
import slick.dbio.DBIO

trait SecurityUserUpdater {

  def updateEmail(securityUser: SecurityUser, newEmail: Email): DBIO[SecurityUser]

  def updatePassword(securityUser: SecurityUser, newPassword: PlainTextPassword): DBIO[SecurityUser]

}