package articles.exceptions

import commons.models.BaseId
import users.models.UserId

class AuthorMismatchException(notAuthorId: UserId, modelId: BaseId[_])
  extends RuntimeException(s"user $notAuthorId is not author of $modelId")