
package articles.repositories

import articles.models.{Tag => _, _}
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}
import users.models.UserId

import scala.concurrent.ExecutionContext

class FavoriteAssociationRepo(implicit private val ec: ExecutionContext) {
  import FavoriteAssociationTable.favoriteAssociations

  def delete(favoriteAssociationIds: Iterable[FavoriteAssociationId]): DBIO[Int] = {
    if (favoriteAssociationIds == null || favoriteAssociationIds.isEmpty) DBIO.successful(0)
    else favoriteAssociations
      .filter(_.id inSet favoriteAssociationIds)
      .delete
  }

  def delete(favoriteAssociationId: FavoriteAssociationId): DBIO[Int] = {
    if (favoriteAssociationId == null) DBIO.successful(0)
    else favoriteAssociations
      .filter(_.id === favoriteAssociationId)
      .delete
  }

  def insert(favoriteAssociation: FavoriteAssociation): DBIO[FavoriteAssociationId] = {
    require(favoriteAssociation != null)

    favoriteAssociations
      .returning(favoriteAssociations.map(_.id)) += favoriteAssociation
  }

  def groupByArticleAndCount(articleIds: Seq[ArticleId]): DBIO[Seq[(ArticleId, Int)]] = {
    require(articleIds != null)

    favoriteAssociations
      .filter(_.favoritedId inSet articleIds)
      .groupBy(_.favoritedId)
      .map({
        case (articleId, q) =>
          (articleId, q.size)
      })
      .result
  }

  def findByUserAndArticles(userId: UserId, articleIds: Seq[ArticleId]): DBIO[Seq[FavoriteAssociation]] = {
    require(articleIds != null)

    favoriteAssociations
      .filter(_.userId === userId)
      .filter(_.favoritedId inSet articleIds)
      .result
  }

  def findByUserAndArticle(userId: UserId, articleId: ArticleId): DBIO[Option[FavoriteAssociation]] = {
    favoriteAssociations
      .filter(_.userId === userId)
      .filter(_.favoritedId === articleId)
      .result
      .headOption
  }

  def findByArticle(articleId: ArticleId): DBIO[Seq[FavoriteAssociation]] = {
    favoriteAssociations
      .filter(_.favoritedId === articleId)
      .result
  }

}

object FavoriteAssociationTable {
  val favoriteAssociations = TableQuery[FavoriteAssociations]

  protected class FavoriteAssociations(tag: Tag)
    extends Table[FavoriteAssociation](tag, "favorite_associations") {

    def id: Rep[FavoriteAssociationId] = column[FavoriteAssociationId]("id", O.PrimaryKey, O.AutoInc)

    def userId: Rep[UserId] = column("user_id")

    def favoritedId: Rep[ArticleId] = column("favorited_id")

    def * : ProvenShape[FavoriteAssociation] = (id, userId, favoritedId) <> ((FavoriteAssociation.apply _).tupled,
      FavoriteAssociation.unapply)
  }
}
