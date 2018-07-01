package users.repositories

import java.time.Instant

import commons.exceptions.MissingModelException
import commons.models.{Email, IdMetaModel, Property, Username}
import commons.repositories._
import commons.repositories.mappings.JavaTimeDbMappings
import commons.utils.DbioUtils
import users.models.{User, UserId, UserMetaModel}
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

class UserRepo(implicit private val ex: ExecutionContext) extends BaseRepo[UserId, User, UserTable] {

  def findByEmailOption(email: Email): DBIO[Option[User]] = {
    require(email != null)

    query
      .filter(_.email === email)
      .result
      .headOption
  }

  def findByEmail(email: Email): DBIO[User] = {
    findByEmailOption(email)
      .flatMap(maybeUser => DbioUtils.optionToDbio(maybeUser, new MissingModelException(s"user with email $email")))
  }

  def findByUsernameOption(username: Username): DBIO[Option[User]] = {
    require(username != null)

    query
      .filter(_.username === username)
      .result
      .headOption
  }

  def findByUsername(username: Username): DBIO[User] = {
    findByUsernameOption(username)
      .flatMap(maybeUser => DbioUtils.optionToDbio(maybeUser,
        new MissingModelException(s"user with username $username")))
  }

  override protected val mappingConstructor: Tag => UserTable = new UserTable(_)

  override protected val modelIdMapping: BaseColumnType[UserId] = UserId.userIdDbMapping

  override protected val metaModel: IdMetaModel = UserMetaModel

  override protected val metaModelToColumnsMapping: Map[Property[_], UserTable => Rep[_]] = Map(
    UserMetaModel.id -> (table => table.id),
    UserMetaModel.username -> (table => table.username)
  )

  implicit val usernameMapping: BaseColumnType[Username] = MappedColumnType.base[Username, String](
    username => username.value,
    str => Username(str)
  )
}

class UserTable(tag: Tag) extends IdTable[UserId, User](tag, "users")
  with JavaTimeDbMappings {

  def username: Rep[Username] = column[Username]("username")

  def email: Rep[Email] = column[Email]("email")

  def bio: Rep[String] = column[String]("bio")

  def image: Rep[String] = column[String]("image")

  def createdAt: Rep[Instant] = column("created_at")

  def updatedAt: Rep[Instant] = column("updated_at")

  def * : ProvenShape[User] = (id, username, email, bio.?, image.?, createdAt, updatedAt) <> ((User.apply _).tupled,
    User.unapply)
}