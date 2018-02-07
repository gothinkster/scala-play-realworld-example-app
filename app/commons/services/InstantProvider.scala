package commons.services

import java.time.Instant

import commons.repositories.DateTimeProvider

class InstantProvider extends DateTimeProvider {

  override def now: Instant = Instant.now

}