package authentication.oauth2.exceptions

private[authentication] class MissingAccessTokenException(token: String) extends RuntimeException(token) {

}