
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

class ArticleTagRepo(tagRepo: TagRepo, implicit private val ec: ExecutionContext)
  extends BaseRepo[ArticleTagId, ArticleTag, ArticleTagTable] {

  def findByArticleId(id: ArticleId): DBIO[Seq[models.Tag]] = {
    findByArticleIds(Seq(id))
      .map(_.map(_.tag))
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

  override protected val mappingConstructor: Tag => ArticleTagTable = new ArticleTagTable(_)

  override protected val modelIdMapping: BaseColumnType[ArticleTagId] = ArticleTagId.articleTagIdDbMapping

  override protected val metaModel: IdMetaModel = ArticleTagMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (ArticleTagTable) => Rep[_]] = Map(
    ArticleTagMetaModel.id -> (table => table.id),
    ArticleTagMetaModel.articleId -> (table => table.articleId),
    ArticleTagMetaModel.tagId -> (table => table.tagId)
  )

}

protected class ArticleTagTable(tag: Tag) extends IdTable[ArticleTagId, ArticleTag](tag, "articles_tags") {

  def articleId: Rep[ArticleId] = column(ArticleTagMetaModel.articleId.name)

  def tagId: Rep[TagId] = column(ArticleTagMetaModel.tagId.name)

  def * : ProvenShape[ArticleTag] = (id, articleId, tagId) <> ((ArticleTag.apply _).tupled, ArticleTag.unapply)

}