package config

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}

import play.api.libs.json._

trait DateTimeJsonMappings {

  implicit val localDateTimeJsonFormat: Format[LocalDateTime] = new Format[LocalDateTime] {

    override def reads(json: JsValue): JsResult[LocalDateTime] = json match {
      case JsString(s) =>
        scala.util.control.Exception.catching(classOf[NumberFormatException])
          .opt(JsSuccess(ZonedDateTime.parse(s).toLocalDateTime))
          .getOrElse(JsError(JsonValidationError("error.expected.numberformatexception")))
      case _ => JsError(JsonValidationError("error.expected.date_time_in_utc_iso_8601_format"))
    }

    override def writes(dateTime: LocalDateTime): JsValue = JsString(dateTime.atZone(ZoneOffset.UTC).toString)
  }
}