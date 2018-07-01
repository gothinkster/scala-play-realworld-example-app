package authentication.api

import slick.dbio.DBIO

trait SecurityUserCreator {
  def create(newSecUser: NewSecurityUser): DBIO[SecurityUser]
}