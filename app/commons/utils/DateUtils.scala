package commons.utils

import java.time.{LocalDateTime, ZoneOffset}
import java.util.Date

object DateUtils {
  val zoneOffset = ZoneOffset.UTC

  def toOldJavaDate(localDateTime: LocalDateTime): Date = {
    if (localDateTime == null) null
    else Date.from(localDateTime.toInstant(zoneOffset))
  }

  def toLocalDateTime(date: Date): LocalDateTime = {
    if (date == null) null
    else LocalDateTime.ofInstant(date.toInstant, zoneOffset)
  }
}
