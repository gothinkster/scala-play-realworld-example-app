package testhelpers

import java.time.LocalDateTime

import commons.repositories.UtcLocalDateTimeProvider

class ProgrammaticDateTimeProvider extends UtcLocalDateTimeProvider {
  override var now: LocalDateTime = super.now
}