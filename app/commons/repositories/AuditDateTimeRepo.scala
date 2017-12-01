package commons.repositories

import java.time.Instant

import commons.models.WithDateTimes
import commons.repositories.mappings.JavaTimeDbMappings
import slick.dbio.DBIO
import slick.jdbc.MySQLProfile.api.{DBIO => _, MappedTo => _, Rep => _, TableQuery => _, _}
import slick.lifted._

trait AuditDateTimeRepo[ModelId <: BaseId[Long],
WithDatesModel <: WithId[Long, ModelId] with WithDateTimes[WithDatesModel],
ModelTable <: IdTable[ModelId, WithDatesModel] with AuditDateTimeTable]
  extends BaseRepo[ModelId, WithDatesModel, ModelTable]
    with JavaTimeDbMappings {
  protected val dateTimeProvider: DateTimeProvider

  override def create(model: WithDatesModel): DBIO[WithDatesModel] = {
    val now = dateTimeProvider.now
    val modelWithDates = model.updateCreatedAt(now)
      .updateUpdatedAt(now)
    super.create(modelWithDates)
  }

  override def update(model: WithDatesModel): DBIO[WithDatesModel] = {
    val now = dateTimeProvider.now
    val modelWithDates = model.updateUpdatedAt(now)
    super.update(modelWithDates)
  }


}

trait AuditDateTimeTable {
  _: Table[_] with JavaTimeDbMappings =>

  def createdAt: Rep[Instant] = column("created_at")

  def updatedAt: Rep[Instant] = column("updated_at")
}