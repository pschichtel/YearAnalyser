package services.event

import java.io.File
import java.nio.charset.Charset
import java.time.Instant.ofEpochMilli
import java.time.{LocalDate, ZoneId}
import java.util.TimeZone

import biweekly.{Biweekly, ICalendar}
import biweekly.io.TimezoneInfo
import biweekly.io.chain.ChainingTextParser
import biweekly.property.DateOrDateTimeProperty
import play.api.libs.ws.WSClient
import services.TextHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class WebICalSource(val name: String, val id: String, client: WSClient, url: String)(private implicit val ec: ExecutionContext) extends DatedEventSource with ICalSource {

    override def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]] = {
        client.url(url).get() map { response =>
            if (response.status == 200) {
                convertICal(timeZone, response.body)
            } else {
                Nil
            }
        }
    }
}

class LocalICalSource(val name: String, val id: String, source: File) extends DatedEventSource with ICalSource {
    private lazy val calendars = parse(source)

    override def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]] = {
        Future.successful(convertICal(timeZone, calendars))
    }
}

trait ICalSource { self: DatedEventSource =>

    def parse(source: File): Seq[ICalendar] =
        getAll(Biweekly.parse(source))

    def parse(source: String): Seq[ICalendar] =
        getAll(Biweekly.parse(source))

    def getAll(parser: ChainingTextParser[?]): Seq[ICalendar] =
        parser.all().asScala.toSeq

    def convertICal(defaultTimezone: TimeZone, source: String): Seq[DatedEvent] =
        convertICal(defaultTimezone, parse(source))

    def convertICal(defaultTimezone: TimeZone, source: File): Seq[DatedEvent] =
        convertICal(defaultTimezone, parse(source))

    def convertICal(defaultTimezone: TimeZone, calendars: Seq[ICalendar]): Seq[DatedEvent] = {
        calendars.map { ical =>
            val icalTimeZoneInfo: TimezoneInfo = ical.getTimezoneInfo
            val icalTimeZone = Option(icalTimeZoneInfo.getTimezones.asScala.headOption.getOrElse(icalTimeZoneInfo.getDefaultTimezone))
                .map(z => ZoneId.of(z.getTimeZone.getID))
                .getOrElse(ZoneId.of(defaultTimezone.getID))
            val dateInZone = toLocalDate(icalTimeZone, _: DateOrDateTimeProperty)

            for (event <- ical.getEvents.asScala.toSeq) yield {
                val start = dateInZone(event.getDateStart)
                val end = dateInZone(event.getDateEnd)
                val name = s"${event.getSummary.getValue} (Source: ${this.name})"
                val id = TextHelper.normalizeString(name, "some-event")
                DatedEvent(id, name, self.id, start, end)
            }
        }.foldLeft(Seq.empty[DatedEvent])(_ ++ _)
    }

    private def toLocalDate(zoneId: ZoneId, prop: DateOrDateTimeProperty): LocalDate =
        ofEpochMilli(prop.getValue.getTime).atZone(zoneId).toLocalDate
}

object ICalSource {
    val GermanCodePage: Charset = Charset.forName("ISO-8859-1")
    val FileExtension: String = ".ics"
}
