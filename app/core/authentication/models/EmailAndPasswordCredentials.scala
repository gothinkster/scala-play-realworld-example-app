package core.authentication.models

import commons.models.Email
import core.authentication.api.PlainTextPassword
import play.api.libs.json.{Format, Json}

private[core] case class EmailAndPasswordCredentials(email: Email, password: PlainTextPassword)

private[core] object EmailAndPasswordCredentials {

  implicit val emailANdPasswordCredentials: Format[EmailAndPasswordCredentials] =
    Json.format[EmailAndPasswordCredentials]

}