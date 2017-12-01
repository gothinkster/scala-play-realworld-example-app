package commons.utils

import java.time.Instant
import java.util.Date

object DateUtils {

  def toOldJavaDate(instant: Instant): Date = {
    if (instant == null) null
    else Date.from(instant)
  }

  def toInstant(date: Date): Instant = {
    if (date == null) null
    else date.toInstant
  }

}
