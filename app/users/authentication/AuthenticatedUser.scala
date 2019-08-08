package users.authentication

import authentication.models.SecurityUserId
import users.models.{User, UserId}

case class AuthenticatedUser(userId: UserId, securityUserId: SecurityUserId, token: String)

object AuthenticatedUser {

  def apply(userAndToken: (User, String)): AuthenticatedUser = {
    val (user, token) = userAndToken
    AuthenticatedUser(user.id, user.securityUserId, token)
  }
}