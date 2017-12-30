
package core.articles.repositories

import commons.models.{IdMetaModel, Property}
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import core.articles.models.{ArticleId, Comment, CommentId, CommentMetaModel}
import core.users.models.UserId
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class CommentRepo(override protected val dateTimeProvider: DateTimeProvider,
                  implicit private val ec: ExecutionContext)
  extends BaseRepo[CommentId, Comment, CommentTable]
    with AuditDateTimeRepo[CommentId, Comment, CommentTable] {

  override protected val mappingConstructor: Tag => CommentTable = new CommentTable(_)

  override protected val modelIdMapping: BaseColumnType[CommentId] = CommentId.commentIdDbMapping

  override protected val metaModel: IdMetaModel = CommentMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (CommentTable) => Rep[_]] = Map(

    CommentMetaModel.id -> (table => table.id),
    CommentMetaModel.articleId -> (table => table.articleId),
    CommentMetaModel.authorId -> (table => table.authorId),
    CommentMetaModel.body -> (table => table.body),
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