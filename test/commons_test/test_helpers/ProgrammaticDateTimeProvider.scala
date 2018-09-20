package commons_test.test_helpers

import java.time.Instant

import commons.services.InstantProvider

class ProgrammaticDateTimeProvider extends InstantProvider {
  var currentTime: Instant = super.now

  override def now: Instant = currentTime
}