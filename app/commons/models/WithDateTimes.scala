package commons.models

import java.time.Instant

trait WithDateTimes[Model] {
  val createdAt: Instant
  val updatedAt: Instant

  def updateCreatedAt(dateTime: Instant): Model

  def updateUpdatedAt(dateTime: Instant): Model
}
