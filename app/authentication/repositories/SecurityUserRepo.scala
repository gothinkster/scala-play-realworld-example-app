package authentication.repositories

import java.time.Instant

import authentication.exceptions.MissingSecurityUserException
import authentication.models.{PasswordHash, SecurityUser, SecurityUserId}
import commons.exceptions.MissingModelException
import commons.models.Email
import commons.utils.DbioUtils
import commons.utils.DbioUtils.optionToDbio
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted.{ProvenShape, _}

import scala.concurrent.ExecutionContext

private[authentication] class SecurityUserRepo(implicit private val ex: ExecutionContext) {

  import SecurityUserTable.securityUsers

  def findByEmailOption(email: Email): DBIO[Option[SecurityUser]] = {
    require(email != null)

    securityUsers
      .filter(_.email === email)
      .result
      .headOption
  }

  def findByEmail(email: Email): DBIO[SecurityUser] = {
    require(email != null)

    findByEmailOption(email)
      .flatMap(optionToDbio(_, new MissingSecurityUserException(email.toString)))
  }

  def insertAndGet(securityUser: SecurityUser): DBIO[SecurityUser] = {
    require(securityUser != null)

    insert(securityUser)
      .flatMap(findById)
  }

  private def insert(securityUser: SecurityUser): DBIO[SecurityUserId] = {
    securityUsers.returning(securityUsers.map(_.id)) += securityUser
  }

  def findById(securityUserId: SecurityUserId): DBIO[SecurityUser] = {
    securityUsers
      .filter(_.id === securityUserId)
      .result
      .headOption
      .flatMap(maybeModel => DbioUtils.optionToDbio(maybeModel, new MissingModelException(s"model id: $securityUserId")))
  }

  def updateAndGet(securityUser: SecurityUser): DBIO[SecurityUser] = {
    update(securityUser).flatMap(_ => findById(securityUser.id))
  }

  private def update(securityUser: SecurityUser): DBIO[Int] = {
    require(securityUser != null)

    securityUsers
      .filter(_.id === securityUser.id)
      .update(securityUser)
  }
}

object SecurityUserTable {
  val securityUsers = TableQuery[SecurityUsers]

  protected class SecurityUsers(tag: Tag) extends Table[SecurityUser](tag, "security_users") {

    def id: Rep[SecurityUserId] = column[SecurityUserId]("id", O.PrimaryKey, O.AutoInc)

    def email: Rep[Email] = column("email")

    def password: Rep[PasswordHash] = column("password")

    def createdAt: Rep[Instant] = column("created_at")

    def updatedAt: Rep[Instant] = column("updated_at")

    def * : ProvenShape[SecurityUser] = (id, email, password, createdAt, updatedAt) <> (SecurityUser.tupled,
      SecurityUser.unapply)
  }

}


