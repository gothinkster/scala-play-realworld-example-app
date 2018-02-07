package commons.repositories

import java.time.Instant

class InstantProvider extends DateTimeProvider {

  override def now: Instant = Instant.now

}