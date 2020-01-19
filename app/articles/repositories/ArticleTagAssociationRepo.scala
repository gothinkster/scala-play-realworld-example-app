
package articles.repositories

import articles.models
import articles.models.{Tag => _, _}
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

case class ArticleIdWithTag(articleId: ArticleId, tag: models.Tag)

class ArticleTagAssociationRepo(implicit private val ec: ExecutionContext) {
  import ArticleTagAssociationTable.articleTagAssociations
  import TagTable.tags

  def findTagsByArticleId(articleId: ArticleId): DBIO[Seq[models.Tag]] = {
    require(articleId != null)

    findByArticleIds(Seq(articleId))
      .map(_.map(_.tag))
  }

  def findByArticleId(articleId: ArticleId): DBIO[Seq[ArticleTagAssociation]] = {
    require(articleId != null)

    articleTagAssociations
      .filter(_.articleId === articleId)
      .result
  }

  def findByArticleIds(articleIds: Seq[ArticleId]): DBIO[Seq[ArticleIdWithTag]] = {
    if (articleIds == null || articleIds.isEmpty) DBIO.successful(Seq.empty)
    else {
      articleTagAssociations
        .filter(_.articleId inSet articleIds)
        .join(tags).on(_.tagId === _.id)
        .map(tables => {
          val (articleTagTable, tagTable) = tables

          (articleTagTable.articleId, tagTable)
        })
        .result
        .map(_.map((ArticleIdWithTag.apply _).tupled))
    }
  }

  def insertAndGet(models: Iterable[ArticleTagAssociation]): DBIO[Seq[ArticleTagAssociation]] = {
    if (models == null && models.isEmpty) DBIO.successful(Seq.empty)
    else articleTagAssociations.returning(articleTagAssociations.map(_.id))
      .++=(models)
      .flatMap(ids => findByIds(ids))
  }

  private def findByIds(modelIds: Iterable[ArticleTagAssociationId]): DBIO[Seq[ArticleTagAssociation]] = {
    if (modelIds == null || modelIds.isEmpty) DBIO.successful(Seq.empty)
    else articleTagAssociations
      .filter(_.id inSet modelIds)
      .result
  }

  def delete(articleTagAssociationIds: Iterable[ArticleTagAssociationId]): DBIO[Int] = {
    if (articleTagAssociationIds == null || articleTagAssociationIds.isEmpty) DBIO.successful(0)
    else articleTagAssociations
      .filter(_.id inSet articleTagAssociationIds)
      .delete
  }

}

object ArticleTagAssociationTable {
  val articleTagAssociations = TableQuery[ArticleTagAssociations]

  protected class ArticleTagAssociations(tag: Tag) extends Table[ArticleTagAssociation](tag, "articles_tags") {

    def id: Rep[ArticleTagAssociationId] = column[ArticleTagAssociationId]("id", O.PrimaryKey, O.AutoInc)

    def articleId: Rep[ArticleId] = column("article_id")

    def tagId: Rep[TagId] = column("tag_id")

    def * : ProvenShape[ArticleTagAssociation] = (id, articleId, tagId) <> ((ArticleTagAssociation.apply _).tupled,
      ArticleTagAssociation.unapply)

  }
}

