package articles.repositories

import java.time.Instant

import articles.models.{Tag => _, _}
import commons.exceptions.MissingModelException
import commons.models._
import commons.utils.DbioUtils
import org.apache.commons.lang3.StringUtils
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, Rep, TableQuery}
import users.models.{User, UserId}
import users.repositories.{FollowAssociationRepo, UsersTable}

import scala.concurrent.ExecutionContext

class ArticleRepo(articleTagAssociationRepo: ArticleTagAssociationRepo,
                  followAssociationRepo: FollowAssociationRepo,
                  implicit private val ec: ExecutionContext) {
  import ArticleTable.articles
  import ArticleTagAssociationTable.articleTagAssociations
  import FavoriteAssociationTable.favoriteAssociations
  import TagTable.tags
  import UsersTable.users

  def findBySlugOption(slug: String): DBIO[Option[Article]] = {
    require(StringUtils.isNotBlank(slug))

    articles
      .filter(_.slug === slug)
      .result
      .headOption
  }

  def findBySlug(slug: String): DBIO[Article] = {
    require(StringUtils.isNotBlank(slug))

    findBySlugOption(slug)
      .flatMap(maybeArticle => DbioUtils.optionToDbio(maybeArticle, new MissingModelException(slug)))
  }

  def findByIdWithUser(id: ArticleId): DBIO[(Article, User)] = {
    articles
      .join(users).on(_.authorId === _.id)
      .filter(_._1.id === id)
      .result
      .headOption
      .map(_.get)
  }

  def findPageRequest(pageRequest: ArticlesPageRequest): DBIO[Page[Article]] = {
    def getAll = {
      val rows = articles
        .sortBy(_.createdAt.desc)
        .drop(pageRequest.offset)
        .take(pageRequest.limit)
        .result

      val count = articles
        .size
        .result

      rows.zip(count)
        .map(buildPage)
    }

    def buildPage(rowsWithCount: (Seq[Article], Int)) = {
      Page(rowsWithCount._1, rowsWithCount._2)
    }

    def getByTag(pg: ArticlesByTag) = {
      val queryBase = articles
        .join(articleTagAssociations).on(_.id === _.articleId)
        .join(tags).on((tables, tagTable) => tables._2.tagId === tagTable.id)
        .filter({
          case (_, tagTable) => tagTable.name === pg.tag
        })

      val count = queryBase.size.result

      queryBase
        .map(_._1._1)
        .result.zip(count)
        .map(buildPage)
    }

    def getByAuthor(pg: ArticlesByAuthor) = {
      val queryBase = articles
        .join(users).on(_.authorId === _.id)
        .filter({
          case (_, userTable) => userTable.username === pg.author
        })

      val count = queryBase.size.result

      queryBase
        .map(_._1)
        .result.zip(count)
        .map(buildPage)
    }

    def getByFavorited(pg: ArticlesByFavorited) = {
      val queryBase = articles
        .join(favoriteAssociations).on(_.id === _.favoritedId)
        .join(users).on((tables, userTable) => tables._2.userId === userTable.id)
        .filter({
          case (_, userTable) => userTable.username === pg.favoritedBy
        })

      val count = queryBase.size.result

      queryBase
        .map(_._1._1)
        .result.zip(count)
        .map(buildPage)
    }

    pageRequest match {
      case _: ArticlesAll =>
        getAll
      case pg: ArticlesByTag =>
        getByTag(pg)
      case pg: ArticlesByAuthor =>
        getByAuthor(pg)
      case pg: ArticlesByFavorited =>
        getByFavorited(pg)
    }
  }

  def findByUserFeedPageRequest(pageRequest: UserFeedPageRequest, userId: UserId): DBIO[Page[Article]] = {
    require(pageRequest != null)

    def getFollowedIdsAction = {
      followAssociationRepo.findByFollower(userId)
        .map(_.map(_.followedId))
    }

    def byUserFeedPageRequest(followedIds: Seq[UserId]) = {
      val base = articles
        .join(users).on(_.authorId === _.id)
        .filter(_._2.id inSet followedIds)
        .map(_._1)

      val page = base
        .sortBy(_.createdAt.desc)
        .drop(pageRequest.offset)
        .take(pageRequest.limit)

      page.result
        .zip(base.size.result)
        .map(articlesAndAuthorsWithCount => Page(articlesAndAuthorsWithCount._1, articlesAndAuthorsWithCount._2))
    }

    getFollowedIdsAction
      .flatMap(followedIds => byUserFeedPageRequest(followedIds))
  }

  def insertAndGet(model: Article): DBIO[Article] = {
    require(model != null)

    insertAndGet(Seq(model))
      .map(_.head)
  }

  private def insertAndGet(models: Iterable[Article]): DBIO[Seq[Article]] = {
    if (models == null && models.isEmpty) DBIO.successful(Seq.empty)
    else articles.returning(articles.map(_.id))
      .++=(models)
      .flatMap(ids => findByIds(ids))
  }

  def updateAndGet(article: Article): DBIO[Article] = {
    require(article != null)

    articles
      .filter(_.id === article.id)
      .update(article)
      .flatMap(_ => findById(article.id))
  }

  def findById(articleId: ArticleId): DBIO[Article] = {
    articles
      .filter(_.id === articleId)
      .result
      .headOption
      .flatMap(maybeModel => DbioUtils.optionToDbio(maybeModel, new MissingModelException(s"model id: $articleId")))
  }

  private def findByIds(modelIds: Iterable[ArticleId]): DBIO[Seq[Article]] = {
    if (modelIds == null || modelIds.isEmpty) DBIO.successful(Seq.empty)
    else articles
      .filter(_.id inSet modelIds)
      .result
  }

  def delete(articleId: ArticleId): DBIO[Int] = {
    require(articleId != null)

    articles
      .filter(_.id === articleId)
      .delete
  }

}

object ArticleTable {
  val articles = TableQuery[Articles]

  protected class Articles(tag: Tag) extends Table[Article](tag, "articles") {

    def id: Rep[ArticleId] = column[ArticleId]("id", O.PrimaryKey, O.AutoInc)

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
}
