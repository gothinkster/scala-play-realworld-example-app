package commons.config

import play.api.mvc.ControllerComponents

trait WithControllerComponents {
  def controllerComponents: ControllerComponents
}
