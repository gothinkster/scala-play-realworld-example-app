package commons.controllers

import config.JsonMappings
import play.api.mvc.{AbstractController, ControllerComponents}

abstract class RealWorldAbstractController(controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents)
    with JsonMappings