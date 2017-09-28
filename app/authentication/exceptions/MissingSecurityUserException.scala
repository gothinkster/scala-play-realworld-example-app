package authentication.exceptions

// todo move to outer module
class MissingSecurityUserException(username: String) extends RuntimeException(username)