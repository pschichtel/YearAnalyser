package services.event

import java.nio.charset.Charset
import java.time.Instant.ofEpochMilli
import java.time.{LocalDate, ZoneId}
import java.util.TimeZone

import biweekly.Biweekly
import biweekly.io.TimezoneInfo
import biweekly.property.DateOrDateTimeProperty
import play.api.libs.ws.WSClient
import services.TextHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class ICalSource(client: WSClient, url: String)(private implicit val ec: ExecutionContext) extends DatedEventSource {
    val GermanCodePage: Charset = Charset.forName("ISO-8859-1")

    override def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]] = {
        client.url(url).get() map { response =>
            if (response.status == 200) {
                ICalSource.convertICal(timeZone, response.body)
            } else {
                Nil
            }
        }
    }
}

object ICalSource {

    def convertICal(timeZone: TimeZone, source: String): Seq[DatedEvent] = {
        val ical = Biweekly.parse(source).first()
        val icalTimeZoneInfo: TimezoneInfo = ical.getTimezoneInfo
        val icalTimeZone = Option(icalTimeZoneInfo.getTimezones.asScala.headOption.getOrElse(icalTimeZoneInfo.getDefaultTimezone))
            .map(z => ZoneId.of(z.getTimeZone.getID))
            .getOrElse(ZoneId.of(timeZone.getID))
        val dateInZone = toLocalDate(icalTimeZone, _: DateOrDateTimeProperty)

        for (event <- ical.getEvents.asScala.toSeq) yield {
            val start = dateInZone(event.getDateStart)
            val end = dateInZone(event.getDateEnd)
            val name = event.getSummary.getValue
            val id = TextHelper.normalizeString(name, "some-event")
            DatedEvent(id, name, start, end)
        }
    }

    private def toLocalDate(zoneId: ZoneId, prop: DateOrDateTimeProperty): LocalDate =
        ofEpochMilli(prop.getValue.getTime).atZone(zoneId).toLocalDate
}
