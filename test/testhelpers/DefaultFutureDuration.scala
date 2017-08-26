package testhelpers

import scala.concurrent.duration.{Duration, DurationInt}

trait DefaultFutureDuration {
  protected implicit val duration: Duration = new DurationInt(5).seconds
}
