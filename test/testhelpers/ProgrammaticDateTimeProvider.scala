package testhelpers

import java.time.Instant

import commons.repositories.InstantProvider

class ProgrammaticDateTimeProvider extends InstantProvider {
  var currentTime: Instant = super.now

  override def now: Instant = currentTime
}