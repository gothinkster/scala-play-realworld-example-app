package core.users.services.exceptions

import commons.models.Username

class MissingUserException(username: Username) extends RuntimeException(username.toString)