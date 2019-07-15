package services.event

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}
import java.util.TimeZone

import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

class SchulferienOrg(conf: Configuration, locations: Map[String, Seq[String]])(private implicit val ec: ExecutionContext) extends DatedEventSource {

    private val basePath: Path = Paths.get(conf.get[String]("data-base"))

    override def getEvents(year: Int, timeZone: TimeZone): Future[Seq[DatedEvent]] = {
        val urls = locations.toSeq.flatMap {case (country, parts) =>
            parts.map(p => SchulferienOrg.buildPath(basePath, country, "ferien_" + p, year)) :+ SchulferienOrg.buildPath(basePath, country, "feiertage", year)
        }


        Future {
            urls.flatMap(path => ICalSource.convertICal(timeZone, readFile(path)))
        }
    }

    def readFile(path: Path): String = {
        new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
    }
}

object SchulferienOrg {
    def buildPath(base: Path, country: String, file: String, year: Int): Path = base.resolve(country).resolve(s"${file}_$year.ics")
}
