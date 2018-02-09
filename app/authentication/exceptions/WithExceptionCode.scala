package authentication.exceptions

import commons.models.ExceptionCode

private[authentication] class WithExceptionCode(val exceptionCode: ExceptionCode)
  extends RuntimeException(exceptionCode.toString)