package authentication.api

import authentication.models.{NewSecurityUser, SecurityUser}
import slick.dbio.DBIO

trait SecurityUserCreator {
  def create(newSecUser: NewSecurityUser): DBIO[SecurityUser]
}