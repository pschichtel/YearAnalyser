package services

import java.time.{DayOfWeek, LocalDate}
import java.time.LocalDate.ofYearDay
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.IsoFields
import java.util.TimeZone

import javax.inject._
import play.api.libs.json.{Format, Json}
import services.event.{DatedEvent, DatedEventSource}

import scala.concurrent.{ExecutionContext, Future}

case class Year(year: Int, today: LocalDate, currentWeekOfYear: Int, days: Seq[Day])
case class Day(date: LocalDate, dayOfWeek: Int, weekOfYear: Int, events: Seq[DatedEvent])
case class Event(id: String, name: String)

object Year {
    implicit val format: Format[Year] = Json.format
}

object Day {
    implicit val format: Format[Day] = Json.format
}

@Singleton
class YearAnalyser @Inject() (timeZone: TimeZone) {

    def getCurrentYear(sources: Seq[DatedEventSource])(implicit ec: ExecutionContext): Future[Year] = {
        getYear(LocalDate.now().getYear, sources)(ec)
    }

    def getYear(year: Int, sources: Seq[DatedEventSource])(implicit ec: ExecutionContext): Future[Year] = {

        Future.sequence(sources.map(_.getEvents(year, timeZone))).map(_.flatten.toVector).map { events =>
            val days = YearAnalyser.daysOfYear(year).map { date =>
                val weekOfYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                val dayOfWeek = date.getDayOfWeek.getValue
                Day(date, dayOfWeek, weekOfYear, events.filter(_.isWithin(date)))
            }

            val today = LocalDate.now()
            Year(year, today, today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), days)
        }

    }
}

object YearAnalyser {
    def daysOfYear(year: Int): LazyList[LocalDate] = daysFrom(ofYearDay(year, 1)).takeWhile(_.getYear == year)

    def daysFrom(start: LocalDate): LazyList[LocalDate] = start #:: daysFrom(start).map(_.plus(1, DAYS))
}