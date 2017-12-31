package core.articles.exceptions

import core.articles.models.CommentId

class MissingCommentException(id: CommentId) extends RuntimeException(s"missing with id: $id")