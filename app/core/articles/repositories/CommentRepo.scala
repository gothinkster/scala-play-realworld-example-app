
package core.articles.repositories

import commons.models.{Descending, IdMetaModel, Ordering, Property}
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import core.articles.models.{Tag => _, _}
import core.users.models.{User, UserId}
import core.users.repositories.UserRepo
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class CommentRepo(userRepo: UserRepo,
                  override protected val dateTimeProvider: DateTimeProvider,
                  implicit private val ec: ExecutionContext)
  extends BaseRepo[CommentId, Comment, CommentTable]
    with AuditDateTimeRepo[CommentId, Comment, CommentTable] {

  def byArticleIdWithAuthor(articleId: ArticleId): DBIO[Seq[(Comment, User)]] = {
    query
      .join(userRepo.query).on(_.authorId === _.id)
      .filter(_._1.articleId === articleId)
      .sortBy(tables => toSlickOrderingSupplier(Ordering(CommentMetaModel.createdAt, Descending))(tables._1))
      .result
  }

  override protected val mappingConstructor: Tag => CommentTable = new CommentTable(_)

  override protected val modelIdMapping: BaseColumnType[CommentId] = CommentId.commentIdDbMapping

  override protected val metaModel: IdMetaModel = CommentMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (CommentTable) => Rep[_]] = Map(

    CommentMetaModel.id -> (table => table.id),
    CommentMetaModel.articleId -> (table => table.articleId),
    CommentMetaModel.authorId -> (table => table.authorId),
    CommentMetaModel.body -> (table => table.body),
    CommentMetaModel.createdAt -> (table => table.createdAt),
    CommentMetaModel.updatedAt -> (table => table.updatedAt)
  )

}

protected class CommentTable(tag: Tag) extends IdTable[CommentId, Comment](tag, "comments")
  with AuditDateTimeTable
  with JavaTimeDbMappings {

  def articleId: Rep[ArticleId] = column("article_id")

  def authorId: Rep[UserId] = column("author_id")

  def body: Rep[String] = column("body")

  def * : ProvenShape[Comment] = (id, articleId, authorId, body, createdAt, updatedAt) <> ((Comment.apply _).tupled,
    Comment.unapply)
}