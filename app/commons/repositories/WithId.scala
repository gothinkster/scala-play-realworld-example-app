package commons.repositories

trait WithId[Underlying, Id <: BaseId[Underlying]] {

  def id: Id

}
