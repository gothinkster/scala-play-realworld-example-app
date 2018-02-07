package core.authentication.models

import play.api.libs.json.{Format, Json}

private[core] case class CredentialsWrapper(user: EmailAndPasswordCredentials)

object CredentialsWrapper {

  implicit val credentialsWrapperFormat: Format[CredentialsWrapper] = Json.format[CredentialsWrapper]

}