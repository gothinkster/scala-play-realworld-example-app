package commons.repositories

import java.time.{Clock, Instant}

class UtcInstantProvider extends DateTimeProvider {
  private val utcClock: Clock = Clock.systemUTC()

  override def now: Instant = Instant.now(utcClock)

}