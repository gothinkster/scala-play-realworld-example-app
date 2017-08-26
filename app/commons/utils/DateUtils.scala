package commons.utils

import java.time.{LocalDateTime, ZoneOffset}
import java.util.Date

object DateUtils {
  private val zoneOffset = ZoneOffset.UTC

  def toOldJavaDate(localDateTime: LocalDateTime): Date = {
    val instant = localDateTime.toInstant(zoneOffset)
    Date.from(instant)
  }
}
