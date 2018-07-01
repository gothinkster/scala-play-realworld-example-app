package authentication.api

class InvalidPasswordException(username: String) extends RuntimeException(s"username: $username")