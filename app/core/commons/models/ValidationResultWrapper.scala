package core.commons.models

case class ValidationResultWrapper(errors: Map[String, Seq[String]])