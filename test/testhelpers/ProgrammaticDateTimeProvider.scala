package testhelpers

import java.time.Instant

import commons.repositories.UtcInstantProvider

class ProgrammaticDateTimeProvider extends UtcInstantProvider {
  var currentTime: Instant = super.now

  override def now: Instant = currentTime
}