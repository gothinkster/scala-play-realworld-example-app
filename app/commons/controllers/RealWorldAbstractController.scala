package commons.controllers

import commons.exceptions.ValidationException
import commons.models.ValidationResultWrapper
import play.api.libs.json.{JsError, Json, Reads}
import play.api.mvc.{AbstractController, BodyParser, ControllerComponents, Result}

import scala.concurrent.ExecutionContext

abstract class RealWorldAbstractController(controllerComponents: ControllerComponents)
  extends AbstractController(controllerComponents) {

  implicit protected val executionContext: ExecutionContext = defaultExecutionContext

  protected def validateJson[A: Reads]: BodyParser[A] = parse.json.validate(
    _.validate[A].asEither.left.map(e => BadRequest(JsError.toJson(e)))
  )

  protected def handleFailedValidation: PartialFunction[Throwable, Result] = {
    case e: ValidationException =>
      val errors = e.violations
        .groupBy(_.property)
        .view
        .mapValues(_.map(propertyViolation => propertyViolation.violation.message))
        .toMap

      val wrapper: ValidationResultWrapper = ValidationResultWrapper(errors)
      UnprocessableEntity(Json.toJson(wrapper))
  }

}