package commons.repositories

import java.time.LocalDateTime

import commons.utils.DateUtils

class UtcLocalDateTimeProvider extends DateTimeProvider {
  private val zoneOffset = DateUtils.zoneOffset

  override def now: LocalDateTime = LocalDateTime.now(zoneOffset)

}