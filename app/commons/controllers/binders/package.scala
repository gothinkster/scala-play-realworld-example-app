package commons.controllers

import commons.models.Login
import play.api.mvc.{PathBindable}

import scala.util.Right

package object binders {
  implicit def loginBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[Login] {

    override def bind(key: String, value: String) : Either[String, Login] = {
      stringBinder.bind("login", value) match {
        case Right(login) => Right(Login(login))
        case _ => Left("Unable to bind login.")
      }
    }

    override def unbind(key: String, login: Login): String = stringBinder.unbind("login", login.value)

  }
}
