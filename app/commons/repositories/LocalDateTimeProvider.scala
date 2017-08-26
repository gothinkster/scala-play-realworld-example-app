package commons.repositories

import java.time.{Clock, LocalDateTime}

class LocalDateTimeProvider extends DateTimeProvider {
  private val utcClock = Clock.systemUTC()

  override def now: LocalDateTime = LocalDateTime.now(utcClock)

}