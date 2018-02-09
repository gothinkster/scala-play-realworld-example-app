package core.authentication.api

class MissingSecurityUserException(username: String) extends RuntimeException(username)