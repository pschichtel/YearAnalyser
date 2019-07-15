package services.event

import java.time.LocalDate
import java.util.TimeZone

import play.api.libs.json.{Format, Json}

import scala.concurrent.Future

trait DatedEventSource {
    def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]]
}

case class DatedEvent(id: String, name: String, start: LocalDate, end: LocalDate) {
    def isWithin(date: LocalDate): Boolean = (date.isAfter(start) || date.isEqual(start)) && date.isBefore(end)
}

object DatedEvent {
    implicit val format: Format[DatedEvent] = Json.format
}