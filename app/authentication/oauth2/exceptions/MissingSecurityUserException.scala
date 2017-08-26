package authentication.oauth2.exceptions

private[authentication] class MissingSecurityUserException(token: String) extends RuntimeException(token) {

}