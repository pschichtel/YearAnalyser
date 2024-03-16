package services.event

import java.time.LocalDate.of
import java.time.Month
import java.time.Month.{DECEMBER, JANUARY, NOVEMBER}
import java.util.TimeZone

import scala.concurrent.Future
import scala.concurrent.Future.successful

object EuropaParkSeason extends DatedEventSource {
    val name = "Europa Park"
    val id = "europapark"
    val EuropaParkPreId = "europapark-pre-season"
    val EuropaParkPostId = "europapark-post-season"

    override def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]] = successful(Seq(
        DatedEvent(EuropaParkPreId, "Not open yet", id, of(year, JANUARY, 1), of(year, Month.MARCH, 23)),
        DatedEvent(EuropaParkPostId, "Already closed", id, of(year, NOVEMBER, 6), of(year, DECEMBER, 31))
    ))
}
