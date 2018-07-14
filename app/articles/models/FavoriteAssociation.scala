
package articles.models

import commons.models.{BaseId, IdMetaModel, Property, WithId}
import users.models.UserId
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}

case class FavoriteAssociation(id: FavoriteAssociationId, userId: UserId, favoritedId: ArticleId)
  extends WithId[Long, FavoriteAssociationId]

case class FavoriteAssociationId(value: Long) extends AnyVal with BaseId[Long]

object FavoriteAssociationId {

  implicit val favoriteAssociationIdDbMapping: BaseColumnType[FavoriteAssociationId] =
    MappedColumnType.base[FavoriteAssociationId, Long](
      vo => vo.value,
      id => FavoriteAssociationId(id)
    )

}

object FavoriteAssociationMetaModel extends IdMetaModel {

  val userId: Property[UserId] = Property("userId")
  val favoritedId: Property[ArticleId] = Property("favoritedId")

  override type ModelId = FavoriteAssociationId
}