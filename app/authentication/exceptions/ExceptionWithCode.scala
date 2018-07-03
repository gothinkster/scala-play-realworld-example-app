package authentication.exceptions

private[authentication] class ExceptionWithCode(val exceptionCode: AuthenticationExceptionCode)
  extends RuntimeException(exceptionCode.toString)