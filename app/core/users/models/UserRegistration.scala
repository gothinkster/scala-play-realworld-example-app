package core.users.models

import commons.models.{Email, Username}
import core.authentication.api.PlainTextPassword

private[users] case class UserRegistration(username: Username, password: PlainTextPassword, email: Email)