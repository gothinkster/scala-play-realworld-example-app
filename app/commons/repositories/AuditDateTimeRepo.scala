package commons.repositories

import java.time.Instant

import commons.models.WithDateTimes
import commons.repositories.mappings.JavaTimeDbMappings
import slick.dbio.DBIO
import slick.jdbc.H2Profile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted._

trait AuditDateTimeRepo[ModelId <: BaseId[Long],
WithDatesModel <: WithId[Long, ModelId] with WithDateTimes[WithDatesModel],
ModelTable <: IdTable[ModelId, WithDatesModel] with AuditDateTimeTable]
  extends BaseRepo[ModelId, WithDatesModel, ModelTable]
    with JavaTimeDbMappings {
  protected val dateTimeProvider: DateTimeProvider

  override def insertAndGet(model: WithDatesModel): DBIO[WithDatesModel] = {
    val now = dateTimeProvider.now
    val modelWithDates = model.updateCreatedAt(now)
      .updateUpdatedAt(now)
    super.insertAndGet(modelWithDates)
  }

  override def updateAndGet(model: WithDatesModel): DBIO[WithDatesModel] = {
    val now = dateTimeProvider.now
    val modelWithDates = model.updateUpdatedAt(now)
    super.updateAndGet(modelWithDates)
  }

}

trait AuditDateTimeTable {
  _: Table[_] with JavaTimeDbMappings =>

  def createdAt: Rep[Instant] = column("created_at")

  def updatedAt: Rep[Instant] = column("updated_at")
}