
package articles.repositories

import java.time.Instant

import articles.models.{Tag => _, _}
import commons.exceptions.MissingModelException
import commons.utils.DbioUtils
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}
import users.models.UserId
import users.repositories.UserRepo

import scala.concurrent.ExecutionContext

class CommentRepo(userRepo: UserRepo, implicit private val ec: ExecutionContext) {
  import CommentTable.comments

  def findByArticleId(articleId: ArticleId): DBIO[Seq[Comment]] = {
    comments
      .filter(_.articleId === articleId)
      .sortBy(_.createdAt.desc)
      .result
  }

  def insertAndGet(comment: Comment): DBIO[Comment] = {
    require(comment != null)

    insert(comment)
      .flatMap(findById)
  }

  private def insert(comment: Comment): DBIO[CommentId] = {
    comments.returning(comments.map(_.id)) += comment
  }

  def findById(commentId: CommentId): DBIO[Comment] = {
    comments
      .filter(_.id === commentId)
      .result
      .headOption
      .flatMap(maybeModel => DbioUtils.optionToDbio(maybeModel, new MissingModelException(s"model id: $commentId")))
  }

  def delete(commentId: CommentId): DBIO[Int] = {
    comments
      .filter(_.id === commentId)
      .delete
  }

  def delete(commentIds: Seq[CommentId]): DBIO[Int] = {
    comments
      .filter(_.id inSet commentIds)
      .delete
  }

}

object CommentTable {
  val comments = TableQuery[Comments]

  protected class Comments(tag: Tag) extends Table[Comment](tag, "comments") {

    def id: Rep[CommentId] = column[CommentId]("id", O.PrimaryKey, O.AutoInc)

    def articleId: Rep[ArticleId] = column("article_id")

    def authorId: Rep[UserId] = column("author_id")

    def body: Rep[String] = column("body")

    def createdAt: Rep[Instant] = column("created_at")

    def updatedAt: Rep[Instant] = column("updated_at")

    def * : ProvenShape[Comment] = (id, articleId, authorId, body, createdAt, updatedAt) <> ((Comment.apply _).tupled,
      Comment.unapply)
  }
}
