package authentication.api

import authentication.models.{PlainTextPassword, SecurityUser, SecurityUserId}
import commons.models.Email
import slick.dbio.DBIO

case class SecurityUserUpdate(email: Option[Email], password: Option[PlainTextPassword])

trait SecurityUserUpdater {

  def update(securityUserId: SecurityUserId, securityUserUpdate: SecurityUserUpdate): DBIO[SecurityUser]

}