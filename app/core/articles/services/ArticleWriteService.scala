package core.articles.services

import commons.repositories.DateTimeProvider
import core.articles.repositories._
import core.users.repositories.UserRepo

import scala.concurrent.ExecutionContext

class ArticleWriteService(override protected val articleRepo: ArticleRepo,
                          protected val articleTagAssociationRepo: ArticleTagAssociationRepo,
                          override protected val tagRepo: TagRepo,
                          override protected val dateTimeProvider: DateTimeProvider,
                          override protected val articleWithTagsRepo: ArticleWithTagsRepo,
                          protected val favoriteAssociationRepo: FavoriteAssociationRepo,
                          override protected val userRepo: UserRepo,
                          protected val commentRepo: CommentRepo,
                          implicit protected val ex: ExecutionContext)
  extends ArticleCreateUpdateService
    with ArticleFavoriteService
    with ArticleDeleteService {
}
