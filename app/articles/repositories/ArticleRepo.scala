package articles.repositories

import articles.controllers.{Page, PageRequest}
import articles.models.{Article, ArticleId, ArticleMetaModel}
import commons.models.{IdMetaModel, Property}
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class ArticleRepo(override protected val dateTimeProvider: DateTimeProvider,
                  implicit private val ec: ExecutionContext)
  extends BaseRepo[ArticleId, Article, ArticleTable]
  with AuditDateTimeRepo[ArticleId, Article, ArticleTable] {

  def byPageRequest(pageRequest: PageRequest): DBIO[Page[Article]] = {
    require(pageRequest != null)

    val count = query.size.result

    val slickOrderings = pageRequest.orderings.map(toSlickOrderingSupplier).reverse

    var articlesQuery = query
      .drop(pageRequest.offset)
      .take(pageRequest.limit)
      .sortBy(slickOrderings.head)

    slickOrderings.tail.foreach(getSlickOrdering => {
      articlesQuery = articlesQuery.sortBy(getSlickOrdering)
    })

    articlesQuery.result.zip(count)
      .map(articlesAndCount => Page(articlesAndCount._1, articlesAndCount._2))
  }


  override protected val mappingConstructor: Tag => ArticleTable = new ArticleTable(_)

  override protected val modelIdMapping: BaseColumnType[ArticleId] = MappedColumnType.base[ArticleId, Long](
    vo => vo.value,
    id => ArticleId(id)
  )

  override protected val metaModel: IdMetaModel = ArticleMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (ArticleTable) => Rep[_]] = Map(
    ArticleMetaModel.id -> (table => table.id),
    ArticleMetaModel.modifiedAt -> (table => table.modifiedAt),
  )

}

protected class ArticleTable(tag: Tag) extends IdTable[ArticleId, Article](tag, "articles")
  with AuditDateTimeTable
  with JavaTimeDbMappings {

  def slug: Rep[String] = column(ArticleMetaModel.slug.name)
  def title: Rep[String] = column(ArticleMetaModel.title.name)
  def description: Rep[String] = column(ArticleMetaModel.description.name)
  def body: Rep[String] = column(ArticleMetaModel.body.name)

  def * : ProvenShape[Article] = (id, slug, title, description, body, createdAt, modifiedAt) <> (Article.tupled,
    Article.unapply)
}
