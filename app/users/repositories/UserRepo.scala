package users.repositories

import java.time.Instant

import authentication.models.SecurityUserId
import commons.exceptions.MissingModelException
import commons.models.{Email, Username}
import commons.utils.DbioUtils
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}
import users.models.{User, UserId}

import scala.concurrent.ExecutionContext

class UserRepo(implicit private val ec: ExecutionContext) {
  import UsersTable.users

  def findById(userId: UserId): DBIO[User] = {
    users
      .filter(_.id === userId)
      .result
      .headOption
      .flatMap(maybeUser => DbioUtils.optionToDbio(maybeUser, new MissingModelException(s"User id: $userId")))
  }

  def findByIds(modelIds: Iterable[UserId]): DBIO[Seq[User]] = {
    if (modelIds == null || modelIds.isEmpty) DBIO.successful(Seq.empty)
    else users
      .filter(_.id inSet modelIds)
      .result
  }

  def findBySecurityUserIdOption(securityUserId: SecurityUserId): DBIO[Option[User]] = {
    require(securityUserId != null)

    users
      .filter(_.securityUserId === securityUserId)
      .result
      .headOption
  }

  def findBySecurityUserId(securityUserId: SecurityUserId): DBIO[User] = {
    findBySecurityUserIdOption(securityUserId)
      .flatMap(maybeUser => DbioUtils.optionToDbio(maybeUser,
        new MissingModelException(s"user with security user id $securityUserId")))
  }

  def findByEmailOption(email: Email): DBIO[Option[User]] = {
    require(email != null)

    users
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

    users
      .filter(_.username === username)
      .result
      .headOption
  }

  def findByUsername(username: Username): DBIO[User] = {
    findByUsernameOption(username)
      .flatMap(maybeUser => DbioUtils.optionToDbio(maybeUser,
        new MissingModelException(s"user with username $username")))
  }

  def insertAndGet(user: User): DBIO[User] = {
    insert(user).flatMap(findById)
  }

  def updateAndGet(user: User): DBIO[User] = {
    update(user).flatMap(_ => findById(user.id))
  }

  private def update(user: User): DBIO[Int] = {
    require(user != null)

    users
      .filter(_.id === user.id)
      .update(user)
  }

  private def insert(user: User): DBIO[UserId] = {
    require(user != null)

    users.returning(users.map(_.id)) += user
  }
}

object UsersTable {
  val users = TableQuery[Users]

  class Users(tag: Tag) extends Table[User](tag, "users") {

    def id: Rep[UserId] = column[UserId]("id", O.PrimaryKey, O.AutoInc)

    def securityUserId: Rep[SecurityUserId] = column[SecurityUserId]("security_user_id")

    def username: Rep[Username] = column[Username]("username")

    def email: Rep[Email] = column[Email]("email")

    def bio: Rep[String] = column[String]("bio")

    def image: Rep[String] = column[String]("image")

    def createdAt: Rep[Instant] = column("created_at")

    def updatedAt: Rep[Instant] = column("updated_at")

    def * : ProvenShape[User] = (id, securityUserId, username, email, bio.?, image.?, createdAt, updatedAt) <> ((User.apply _).tupled,
      User.unapply)
  }
}
