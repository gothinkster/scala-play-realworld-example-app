
package core.articles.repositories

import commons.models.{IdMetaModel, Property}
import commons.repositories._
import core.articles.models
import core.articles.models.{Tag => _, _}
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

case class ArticleIdWithTag(articleId: ArticleId, tag: models.Tag)

class ArticleTagAssociationRepo(tagRepo: TagRepo, implicit private val ec: ExecutionContext)
  extends BaseRepo[ArticleTagAssociationId, ArticleTagAssociation, ArticleTagAssociationTable] {

  def findTagsByArticleId(articleId: ArticleId): DBIO[Seq[models.Tag]] = {
    require(articleId != null)

    findByArticleIds(Seq(articleId))
      .map(_.map(_.tag))
  }

  def findByArticleId(articleId: ArticleId): DBIO[Seq[ArticleTagAssociation]] = {
    require(articleId != null)

    query
      .filter(_.articleId === articleId)
      .result
  }

  def findByArticleIds(articleIds: Seq[ArticleId]): DBIO[Seq[ArticleIdWithTag]] = {
    if (articleIds == null || articleIds.isEmpty) DBIO.successful(Seq.empty)
    else {
      query
        .filter(_.articleId inSet articleIds)
        .join(tagRepo.query).on(_.tagId === _.id)
        .map(tables => {
          val (articleTagTable, tagTable) = tables

          (articleTagTable.articleId, tagTable)
        })
        .result
        .map(_.map((ArticleIdWithTag.apply _).tupled))
    }
  }

  override protected val mappingConstructor: Tag => ArticleTagAssociationTable = new ArticleTagAssociationTable(_)

  override protected val modelIdMapping: BaseColumnType[ArticleTagAssociationId] = ArticleTagAssociationId.articleTagAssociationIdDbMapping

  override protected val metaModel: IdMetaModel = ArticleTagAssociationMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], ArticleTagAssociationTable => Rep[_]] = Map(
    ArticleTagAssociationMetaModel.id -> (table => table.id),
    ArticleTagAssociationMetaModel.articleId -> (table => table.articleId),
    ArticleTagAssociationMetaModel.tagId -> (table => table.tagId)
  )

}

protected class ArticleTagAssociationTable(tag: Tag)
  extends IdTable[ArticleTagAssociationId, ArticleTagAssociation](tag, "articles_tags") {

  def articleId: Rep[ArticleId] = column("article_id")

  def tagId: Rep[TagId] = column("tag_id")

  def * : ProvenShape[ArticleTagAssociation] = (id, articleId, tagId) <> ((ArticleTagAssociation.apply _).tupled,
    ArticleTagAssociation.unapply)

}