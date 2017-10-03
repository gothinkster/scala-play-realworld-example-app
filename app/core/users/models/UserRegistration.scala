package core.users.models

import commons.models.Login
import core.authentication.api.PlainTextPassword

private[users] case class UserRegistration(username: Login, password: PlainTextPassword)