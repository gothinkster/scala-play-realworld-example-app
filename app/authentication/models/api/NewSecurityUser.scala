package authentication.models.api

import commons.models.Login

case class NewSecurityUser(login: Login, password: PlainTextPassword)

case class PlainTextPassword(value: String) extends AnyVal