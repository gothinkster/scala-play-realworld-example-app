package testhelpers

import java.time.Instant

import commons.repositories.DateTimeProvider

class FixedDateTimeProvider(dateTime: Instant) extends DateTimeProvider {
  override def now: Instant = dateTime
}