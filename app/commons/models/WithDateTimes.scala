package commons.models

import java.time.LocalDateTime

trait WithDateTimes[Model] {
  val createdAt: LocalDateTime
  val modifiedAt: LocalDateTime

  def updateCreatedAt(dateTime: LocalDateTime): Model

  def updateModifiedAt(dateTime: LocalDateTime): Model
}
