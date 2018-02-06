
package core.articles.repositories

import commons.models.{IdMetaModel, Property}
import commons.repositories._
import core.articles.models.{ArticleId, FavoriteAssociation, FavoriteAssociationId, FavoriteAssociationMetaModel}
import core.users.models.UserId
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class FavoriteAssociationRepo(implicit private val ec: ExecutionContext)
  extends BaseRepo[FavoriteAssociationId, FavoriteAssociation, FavoriteAssociationTable] {

  def groupByArticleAndCount(articleIds: Seq[ArticleId]): DBIO[Seq[(ArticleId, Int)]] = {
    require(articleIds != null)

    query
      .filter(_.favoritedId inSet articleIds)
      .groupBy(_.favoritedId)
      .map({
        case (articleId, q) =>
          (articleId, q.size)
      })
      .result
  }

  def byUserAndArticles(userId: UserId, articleIds: Seq[ArticleId]): DBIO[Seq[FavoriteAssociation]] = {
    require(articleIds != null)

    query
      .filter(_.userId === userId)
      .filter(_.favoritedId inSet articleIds)
      .result
  }

  def byUserAndArticle(userId: UserId, articleId: ArticleId): DBIO[Option[FavoriteAssociation]] = {
    query
      .filter(_.userId === userId)
      .filter(_.favoritedId === articleId)
      .result
      .headOption
  }

  def byArticle(articleId: ArticleId): DBIO[Seq[FavoriteAssociation]] = {
    query
      .filter(_.favoritedId === articleId)
      .result
  }

  override protected val mappingConstructor: Tag => FavoriteAssociationTable = new FavoriteAssociationTable(_)

  override protected val modelIdMapping: BaseColumnType[FavoriteAssociationId] =
    FavoriteAssociationId.favoriteAssociationIdDbMapping

  override protected val metaModel: IdMetaModel = FavoriteAssociationMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (FavoriteAssociationTable) => Rep[_]] = Map(
    FavoriteAssociationMetaModel.id -> (table => table.id),
    FavoriteAssociationMetaModel.userId -> (table => table.userId),
    FavoriteAssociationMetaModel.favoritedId -> (table => table.favoritedId),
  )
}

protected class FavoriteAssociationTable(tag: Tag)
  extends IdTable[FavoriteAssociationId, FavoriteAssociation](tag, "favorite_associations") {

  def userId: Rep[UserId] = column("user_id")

  def favoritedId: Rep[ArticleId] = column("favorited_id")

  def * : ProvenShape[FavoriteAssociation] = (id, userId, favoritedId) <> ((FavoriteAssociation.apply _).tupled,
    FavoriteAssociation.unapply)
}