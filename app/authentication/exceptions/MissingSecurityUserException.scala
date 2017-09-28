package authentication.exceptions

class MissingSecurityUserException(username: String) extends RuntimeException(username)