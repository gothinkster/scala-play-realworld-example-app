package core.articles.repositories

import commons.models.{IdMetaModel, Page, PageRequest, Property}
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import core.articles.models.{Article, ArticleId, ArticleMetaModel}
import core.users.models.{User, UserId}
import core.users.repositories.UserRepo
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class ArticleRepo(userRepo: UserRepo,
                  protected val dateTimeProvider: DateTimeProvider,
                  implicit private val ec: ExecutionContext)
  extends BaseRepo[ArticleId, Article, ArticleTable]
  with AuditDateTimeRepo[ArticleId, Article, ArticleTable] {

  def byIdWithUser(id: ArticleId): DBIO[(Article, User)] = {
    query
      .filter(_.id === id)
      .join(userRepo.query).on(_.userId === _.id)
      .result
      .headOption
      .map(_.get)
  }

  def byPageRequest(pageRequest: PageRequest): DBIO[Page[(Article, User)]] = {
    require(pageRequest != null)

    val slickOrderings = pageRequest.orderings.map(toSlickOrderingSupplier).reverse

    var articlesQuery = query
      .join(userRepo.query).on(_.userId === _.id)
      .drop(pageRequest.offset)
      .take(pageRequest.limit)
      .sortBy(tables => slickOrderings.head(tables._1))

    slickOrderings.tail.foreach(someColumn => {
      articlesQuery = articlesQuery.sortBy(tables => someColumn(tables._1))
    })

    articlesQuery.result.zip(query.size.result)
      .map(articlesAndCount => Page(articlesAndCount._1, articlesAndCount._2))
  }

  override protected val mappingConstructor: Tag => ArticleTable = new ArticleTable(_)

  override protected val modelIdMapping: BaseColumnType[ArticleId] = ArticleId.articleIdDbMapping

  override protected val metaModel: IdMetaModel = ArticleMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (ArticleTable) => Rep[_]] = Map(
    ArticleMetaModel.id -> (table => table.id),
    ArticleMetaModel.updatedAt -> (table => table.updatedAt),
  )

}

protected class ArticleTable(tag: Tag) extends IdTable[ArticleId, Article](tag, "articles")
  with AuditDateTimeTable
  with JavaTimeDbMappings {

  def slug: Rep[String] = column(ArticleMetaModel.slug.name)
  def title: Rep[String] = column(ArticleMetaModel.title.name)
  def description: Rep[String] = column(ArticleMetaModel.description.name)
  def body: Rep[String] = column(ArticleMetaModel.body.name)
  def userId: Rep[UserId] = column("user_id")

  def * : ProvenShape[Article] = (id, slug, title, description, body, createdAt, updatedAt, userId) <> (
    (Article.apply _).tupled, Article.unapply)
}
