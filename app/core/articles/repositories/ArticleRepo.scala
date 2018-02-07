package core.articles.repositories

import java.time.Instant

import commons.models.{IdMetaModel, Page, Property}
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import core.articles.models.{Tag => _, _}
import core.users.models.{User, UserId}
import core.users.repositories.{FollowAssociationRepo, UserRepo, UserTable}
import org.apache.commons.lang3.StringUtils
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, Rep}

import scala.concurrent.ExecutionContext

class ArticleRepo(userRepo: UserRepo,
                  articleTagRepo: ArticleTagRepo,
                  tagRepo: TagRepo,
                  followAssociationRepo: FollowAssociationRepo,
                  favoriteAssociation: FavoriteAssociationRepo,
                  implicit private val ec: ExecutionContext) extends BaseRepo[ArticleId, Article, ArticleTable]
  with JavaTimeDbMappings {

  def bySlug(slug: String): DBIO[Option[Article]] = {
    require(StringUtils.isNotBlank(slug))

    query
      .filter(_.slug === slug)
      .result
      .headOption
  }

  def bySlugWithAuthor(slug: String): DBIO[Option[(Article, User)]] = {
    require(StringUtils.isNotBlank(slug))

    query
      .join(userRepo.query).on(_.authorId === _.id)
      .filter(_._1.slug === slug)
      .result
      .headOption
  }

  def byIdWithUser(id: ArticleId): DBIO[(Article, User)] = {
    query
      .join(userRepo.query).on(_.authorId === _.id)
      .filter(_._1.id === id)
      .result
      .headOption
      .map(_.get)
  }

  def byMainFeedPageRequest(pageRequest: MainFeedPageRequest): DBIO[Page[(Article, User)]] = {
    require(pageRequest != null)

    val joinsWithFilters = getQueryBase(pageRequest)

    val count = joinsWithFilters
      .map(tables => getArticleTab(tables).id)
      .distinct
      .size

    val articleIdsAndCreatedAtPage = joinsWithFilters
      .map(tables => {
        val articleTable = getArticleTab(tables)
        (articleTable.id, articleTable.createdAt)
      })
      .distinct
      .sortBy(idAndCreatedAt => idAndCreatedAt._2.desc)
      .drop(pageRequest.offset)
      .take(pageRequest.limit)

    articleIdsAndCreatedAtPage.result.map(_.map(_._1))
      .flatMap(getArticlesWithAuthorsAction)
      .zip(count.result)
      .map(articlesAndAuthorsWithCount => Page(articlesAndAuthorsWithCount._1, articlesAndAuthorsWithCount._2))
  }

  def byUserFeedPageRequest(pageRequest: UserFeedPageRequest, userId: UserId): DBIO[Page[(Article, User)]] = {
    require(pageRequest != null)

    def getFollowedIdsAction = {
      followAssociationRepo.byFollower(userId)
        .map(_.map(_.followedId))
    }

    def byUserFeedPageRequest(followedIds: Seq[UserId]) = {
      val base = query
        .join(userRepo.query).on(_.authorId === _.id)
        .filter(_._2.id inSet followedIds)

      val page = base
        .sortBy(_._1.createdAt.desc)
        .drop(pageRequest.offset)
        .take(pageRequest.limit)

      page.result
        .zip(base.size.result)
        .map(articlesAndAuthorsWithCount => Page(articlesAndAuthorsWithCount._1, articlesAndAuthorsWithCount._2))
    }

    getFollowedIdsAction
      .flatMap(followedIds => byUserFeedPageRequest(followedIds))
  }

  private def getQueryBase(pageRequest: MainFeedPageRequest) = {
    val joins = query
      .join(userRepo.query).on(_.authorId === _.id)
      .joinLeft(articleTagRepo.query).on(_._1.id === _.articleId)
      .joinLeft(tagRepo.query).on((tables, tagTable) => tables._2.map(_.tagId === tagTable.id))
      .joinLeft(favoriteAssociation.query)
      .on((tables, favoritedAssociationTable) => tables._1._1._1.id === favoritedAssociationTable.favoritedId)

    MaybeFilter(joins)
      .filter(pageRequest.author)(authorUsername => tables => getUserTable(tables).username === authorUsername)
      .filter(pageRequest.tag)(tagValue => tables => getTagTable(tables).map(_.name === tagValue))
      .filter(pageRequest.favorited)(favoritedUsername => tables => {
        getFavoritedAssociationTable(tables).map(favoritedAssociationTable => {
          val userTable = getUserTable(tables)
          favoritedAssociationTable.userId === userTable.id && userTable.username === favoritedUsername
        })
      })
      .query
  }

  private def getArticlesWithAuthorsAction(articleIds: Seq[ArticleId]) = {
    query
      .join(userRepo.query).on(_.authorId === _.id)
      .filter(_._1.id inSet articleIds)
      .sortBy(_._1.createdAt.desc)
      .result
  }

  private def getArticleTab(tables: ((((ArticleTable, UserTable), Rep[Option[ArticleTagTable]]), Rep[Option[TagTable]]), Rep[Option[FavoriteAssociationTable]])) = {
    tables._1._1._1._1
  }

  private def getTagTable(tables: ((((ArticleTable, UserTable), Rep[Option[ArticleTagTable]]), Rep[Option[TagTable]]), Rep[Option[FavoriteAssociationTable]])) = {
    tables._1._2
  }

  private def getUserTable(tables: ((((ArticleTable, UserTable), Rep[Option[ArticleTagTable]]), Rep[Option[TagTable]]), Rep[Option[FavoriteAssociationTable]])) = {
    tables._1._1._1._2
  }

  private def getFavoritedAssociationTable(tables: ((((ArticleTable, UserTable), Rep[Option[ArticleTagTable]]), Rep[Option[TagTable]]), Rep[Option[FavoriteAssociationTable]])) = {
    tables._2
  }

  override protected val mappingConstructor: Tag => ArticleTable = new ArticleTable(_)

  override protected val modelIdMapping: BaseColumnType[ArticleId] = ArticleId.articleIdDbMapping

  override protected val metaModel: IdMetaModel = ArticleMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], (ArticleTable) => Rep[_]] = Map(
    ArticleMetaModel.id -> (table => table.id),
    ArticleMetaModel.createdAt -> (table => table.createdAt),
    ArticleMetaModel.updatedAt -> (table => table.updatedAt),
  )

}

protected class ArticleTable(tag: Tag) extends IdTable[ArticleId, Article](tag, "articles")
  with JavaTimeDbMappings {

  def slug: Rep[String] = column(ArticleMetaModel.slug.name)

  def title: Rep[String] = column(ArticleMetaModel.title.name)

  def description: Rep[String] = column(ArticleMetaModel.description.name)

  def body: Rep[String] = column(ArticleMetaModel.body.name)

  def authorId: Rep[UserId] = column("author_id")

  def createdAt: Rep[Instant] = column("created_at")

  def updatedAt: Rep[Instant] = column("updated_at")

  def * : ProvenShape[Article] = (id, slug, title, description, body, createdAt, updatedAt, authorId) <> (
    (Article.apply _).tupled, Article.unapply)
}
