package core.users.models

import java.time.LocalDateTime

import commons.models.{Email, Username}

private[users] case class RegisteredUser(email: Email, username: Username, createdAt: LocalDateTime,
                                         updatedAt: LocalDateTime, token: String)

private[users] case class RegisteredUserWrapper(user: RegisteredUser)