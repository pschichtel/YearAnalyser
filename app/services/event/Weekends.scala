package services.event

import java.time.DayOfWeek
import java.time.DayOfWeek.{SATURDAY, SUNDAY}
import java.time.temporal.ChronoUnit.DAYS
import java.util.TimeZone

import services.YearAnalyser

import scala.concurrent.Future
import scala.concurrent.Future.successful

object Weekends extends DatedEventSource {

    val WeekendDays: Set[DayOfWeek] = Set(SATURDAY, SUNDAY)

    override def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]] = {
        val weekendDays = YearAnalyser.daysOfYear(year)
            .filter(d => WeekendDays.contains(d.getDayOfWeek))

        successful(weekendDays
            .map(date => DatedEvent("weekend", "Weekend", date, date.plus(1, DAYS))).toList)
    }
}
