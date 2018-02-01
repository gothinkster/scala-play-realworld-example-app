package core.users.exceptions

import commons.models.Username

class MissingUserException(username: Username) extends RuntimeException(username.toString)