package testhelpers

import java.time.LocalDateTime

import commons.repositories.UtcLocalDateTimeProvider

class ProgrammaticDateTimeProvider extends UtcLocalDateTimeProvider {
  var currentTime: LocalDateTime = super.now

  override def now: LocalDateTime = currentTime
}