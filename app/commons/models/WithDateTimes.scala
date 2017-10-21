package commons.models

import java.time.LocalDateTime

trait WithDateTimes[Model] {
  val createdAt: LocalDateTime
  val updatedAt: LocalDateTime

  def updateCreatedAt(dateTime: LocalDateTime): Model

  def updateUpdatedAt(dateTime: LocalDateTime): Model
}
