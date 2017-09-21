package authentication.exceptions

private[authentication] class MissingSecurityUserException(username: String) extends RuntimeException(username)