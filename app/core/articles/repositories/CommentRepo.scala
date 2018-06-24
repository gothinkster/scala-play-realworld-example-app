
package core.articles.repositories

import java.time.Instant

import commons.models._
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import core.articles.models.{Tag => _, _}
import core.users.models.{User, UserId}
import core.users.repositories.UserRepo
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class CommentRepo(userRepo: UserRepo, implicit private val ec: ExecutionContext)
  extends BaseRepo[CommentId, Comment, CommentTable] with JavaTimeDbMappings {

  def findByArticleId(articleId: ArticleId): DBIO[Seq[Comment]] = {
    query
      .filter(_.articleId === articleId)
      .sortBy(commentTable => toSlickOrderingSupplier(Ordering(CommentMetaModel.createdAt, Descending))(commentTable))
      .result
  }

  override protected val mappingConstructor: Tag => CommentTable = new CommentTable(_)

  override protected val modelIdMapping: BaseColumnType[CommentId] = CommentId.commentIdDbMapping

  override protected val metaModel: IdMetaModel = CommentMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], CommentTable => Rep[_]] = Map(

    CommentMetaModel.id -> (table => table.id),
    CommentMetaModel.articleId -> (table => table.articleId),
    CommentMetaModel.authorId -> (table => table.authorId),
    CommentMetaModel.body -> (table => table.body),
    CommentMetaModel.createdAt -> (table => table.createdAt),
    CommentMetaModel.updatedAt -> (table => table.updatedAt)
  )

}

protected class CommentTable(tag: Tag) extends IdTable[CommentId, Comment](tag, "comments")
  with JavaTimeDbMappings {

  def articleId: Rep[ArticleId] = column("article_id")

  def authorId: Rep[UserId] = column("author_id")

  def body: Rep[String] = column("body")

  def createdAt: Rep[Instant] = column("created_at")

  def updatedAt: Rep[Instant] = column("updated_at")

  def * : ProvenShape[Comment] = (id, articleId, authorId, body, createdAt, updatedAt) <> ((Comment.apply _).tupled,
    Comment.unapply)
}