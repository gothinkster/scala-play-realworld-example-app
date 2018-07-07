package authentication.api

import authentication.models.{PlainTextPassword, SecurityUser}
import commons.models.Email
import slick.dbio.DBIO

case class SecurityUserUpdate(email: Option[Email], password: Option[PlainTextPassword])

trait SecurityUserUpdater {

  def update(currentEmail: Email, securityUserUpdate: SecurityUserUpdate): DBIO[SecurityUser]

}