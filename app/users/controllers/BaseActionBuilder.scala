package users.controllers

import authentication.exceptions.ExceptionWithCode
import authentication.jwt.services.JwtAuthenticator
import play.api.mvc._
import slick.dbio.DBIO
import users.models.User
import users.repositories.UserRepo

import scala.concurrent.ExecutionContext

private[controllers] class BaseActionBuilder(
                                              jwtAuthenticator: JwtAuthenticator,
                                              userRepo: UserRepo,
                                                                  )(implicit ec: ExecutionContext)
{
  protected def authenticate(requestHeader: RequestHeader): DBIO[(User, String)] = {
    jwtAuthenticator.authenticate(requestHeader)
      .fold(
        authExceptionCode => DBIO.failed(new ExceptionWithCode(authExceptionCode)),
        securityUserIdAndToken => {
          val (securityUserId, token) = securityUserIdAndToken
          userRepo.findBySecurityUserId(securityUserId)
            .map(user => (user, token))
        }
      )
  }

}