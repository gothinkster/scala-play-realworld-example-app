package authentication.services.api

import authentication.models.SecurityUser
import authentication.models.api.NewSecurityUser
import slick.dbio.DBIO

trait SecurityUserCreator {
  def create(newSecUser: NewSecurityUser): DBIO[SecurityUser]
}