package authentication.exceptions

class ExceptionWithCode(val exceptionCode: AuthenticationExceptionCode)
  extends RuntimeException(exceptionCode.toString)