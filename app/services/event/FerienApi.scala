package services.event

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.TimeZone

import play.api.cache.AsyncCacheApi
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.sequence
import scala.concurrent.duration.DurationLong

class FerienApi(cache: AsyncCacheApi, client: WSClient, states: Set[String] = FerienApi.AllStates.keys.toSet) extends DatedEventSource {

    val name = "ferien-api.de"
    val id = "ferien-api"
    private val baseUrl = "https://ferien-api.de/api/v1"
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")

    case class Holidays(name: String, slug: String, year: Int, stateCode: String, start: String, end: String)

    object Holidays {
        implicit val read: Reads[Holidays] = Json.reads
    }

    implicit object LocalDateReads extends Reads[LocalDate] {
        override def reads(json: JsValue): JsResult[LocalDate] = {
            json match {
                case JsString(dateString) =>
                    JsSuccess(LocalDate.parse(dateString, formatter))
                case _ => JsError("string expected")
            }
        }
    }


    override def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]] = {
        cache.getOrElseUpdate(s"ferien-api-$year", 1.second) {
            Future.sequence(states.toSeq.map { state =>
                germanSchoolHolidaysFor(client, state, year).map { holidays =>
                    holidays.map { data =>
                        DatedEvent(s"ferien-api-${data.slug}", s"${data.name} (${FerienApi.AllStates(state)})", id, LocalDate.parse(data.start, formatter), LocalDate.parse(data.end, formatter))
                    }
                }
            }).map(_.flatten)
        }
    }

    private def germanSchoolHolidaysFor(client: WSClient, state: String, year: Int): Future[Seq[Holidays]] = {
        client.url(s"$baseUrl/holidays/$state/$year").get() map { response =>
            if (response.status == 200) response.json.as[Seq[Holidays]]
            else Nil
        }
    }
}

object FerienApi {
    val AllStates: Map[String, String] = Map(
        "BW" -> "Baden-Württemberg",
        "BY" -> "Bayern",
        "BE" -> "Berlin",
        "BB" -> "Brandenburg",
        "HB" -> "Bremen",
        "HH" -> "Hamburg",
        "HE" -> "Hessen",
        "MV" -> "Mecklenburg-Vorpommern",
        "NI" -> "Niedersachsen",
        "NW" -> "Nordrhein-Westfalen",
        "RP" -> "Rheinland-Pfalz",
        "SL" -> "Saarland",
        "SN" -> "Sachsen",
        "ST" -> "Sachsen-Anhalt",
        "SH" -> "Schleswid-Holstein",
        "TH" -> "Thüringen"
    )
}