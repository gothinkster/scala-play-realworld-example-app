package core.users.test_helpers

import commons.repositories.ActionRunner
import core.users.models.User
import core.users.repositories.UserRepo
import testhelpers.Populator

class UserPopulator(userRepo: UserRepo,
                    implicit private val actionRunner: ActionRunner) extends Populator {

  def save(newUser: User): User = {
     runAndAwait(userRepo.insertAndGet(newUser))
  }

}