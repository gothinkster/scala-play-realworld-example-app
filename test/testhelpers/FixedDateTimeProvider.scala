package testhelpers

import java.time.LocalDateTime

import commons.repositories.DateTimeProvider

class FixedDateTimeProvider(dateTime: LocalDateTime) extends DateTimeProvider {
  override def now: LocalDateTime = dateTime
}