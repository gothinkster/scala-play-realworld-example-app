package authentication.jwt.services

import play.api.Configuration

private[authentication] class SecretProvider(config: Configuration) {
  def get: String = config.get[String]("play.http.secret.key")
}