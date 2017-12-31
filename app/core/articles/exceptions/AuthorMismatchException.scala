package core.articles.exceptions

import core.users.models.UserId

class AuthorMismatchException(userId: UserId, authorId: UserId)
  extends RuntimeException(s"current user $userId is not author $authorId")