package core.authentication.api

import commons.models.Email

case class NewSecurityUser(email: Email, password: PlainTextPassword)

case class PlainTextPassword(value: String) extends AnyVal