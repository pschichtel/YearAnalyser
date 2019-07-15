package services.event

import java.time.LocalDate.ofYearDay
import java.time.{LocalDate, ZoneId}
import java.util.TimeZone

import scala.concurrent.Future

object InThePast extends DatedEventSource {
    override def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]] = {
        val today = LocalDate.now(ZoneId.of(timeZone.getID))
        Future.successful(Seq(DatedEvent("in-the-past", "In The Past", ofYearDay(year, 1), today.minusDays(1))))
    }
}
