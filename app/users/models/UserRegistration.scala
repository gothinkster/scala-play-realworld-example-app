package users.models

import authentication.models.api.PlainTextPassword
import commons.models.Login

private[users] case class UserRegistration(login: Login, password: PlainTextPassword)