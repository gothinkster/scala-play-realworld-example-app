package commons.models

trait WithId[Underlying, Id <: BaseId[Underlying]] {

  def id: Id

}
