package authentication.api

class MissingSecurityUserException(username: String) extends RuntimeException(username)