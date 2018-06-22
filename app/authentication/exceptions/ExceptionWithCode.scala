package authentication.exceptions

import commons.models.ExceptionCode

private[authentication] class ExceptionWithCode(val exceptionCode: ExceptionCode)
  extends RuntimeException(exceptionCode.toString)