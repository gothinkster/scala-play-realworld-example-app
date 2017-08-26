package commons.repositories

import java.time.LocalDateTime

trait DateTimeProvider {
  def now: LocalDateTime
}
