package services.event

import java.time.DayOfWeek
import java.time.DayOfWeek.{SATURDAY, SUNDAY}
import java.time.temporal.ChronoUnit.DAYS
import java.util.TimeZone

import services.YearResolver

import scala.concurrent.Future
import scala.concurrent.Future.successful

object Weekends extends DatedEventSource {
    val name = "Weekends"
    val id = "weekends"
    val WeekendDays: Set[DayOfWeek] = Set(SATURDAY, SUNDAY)

    override def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]] = {
        val weekendDays = YearResolver.daysOfYear(year)
            .filter(d => WeekendDays.contains(d.getDayOfWeek))

        successful(weekendDays
            .map(date => DatedEvent("weekend", "Weekend", id, date, date.plus(1, DAYS))).toList)
    }
}
