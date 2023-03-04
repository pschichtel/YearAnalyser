package services

import java.nio.file.{Files, Path, Paths}
import java.time.LocalDate
import java.time.LocalDate.ofYearDay
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.IsoFields
import java.util.TimeZone

import javax.inject._
import play.api.Configuration
import play.api.cache.AsyncCacheApi
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.WSClient
import services.event.{DatedEvent, DatedEventSource, FerienApi, ICalSource, InThePast, LocalICalSource, Weekends}

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

case class Year(year: Int, today: LocalDate, currentWeekOfYear: Int, days: Seq[Day], sources: Set[Source])
case class Source(name: String, id: String)
case class Day(date: LocalDate, dayOfWeek: Int, weekOfYear: Int, events: Seq[DatedEvent])
case class Event(id: String, name: String)

object Year {
    implicit val format: Format[Year] = Json.format
}


object Source {
    implicit val format: Format[Source] = Json.format
}

object Day {
    implicit val format: Format[Day] = Json.format
}

@Singleton
class YearResolver @Inject()(timeZone: TimeZone, cache: AsyncCacheApi, client: WSClient, config: Configuration) {

    private val dataDir = Paths.get(config.get[String]("data-dir"))
    private val schulferienOrgDir = dataDir.resolve("schulferien.org")

    private def staticSources = Seq(Weekends, InThePast, new FerienApi(cache, client))
    private def sourcesPerYear = scanSchulferienOrg()
    private def sources(year: Int) = staticSources ++ sourcesPerYear.getOrElse(year, Seq.empty)

    private def scanSchulferienOrg(): Map[Int, Seq[DatedEventSource]] = {
        listFolders(schulferienOrgDir)
            .flatMap(scanYear)
            .toMap
    }

    private def scanYear(path: Path): Option[(Int, Seq[DatedEventSource])] = {
        path.getFileName.toString.toIntOption.map { year =>
            (year, listFolders(path)
                .flatMap(scanCountry(year)))
        }
    }

    private def scanCountry(year: Int)(path: Path): Seq[DatedEventSource] = {
        val countryName = path.getFileName.toString
        listFiles(path, ICalSource.FileExtension)
            .map(convertCountryCalendar(year, countryName))
    }

    private def convertCountryCalendar(year: Int, countryName: String)(path: Path): DatedEventSource = {
        val fullFileName = path.getFileName.toString
        val fileName = fullFileName.substring(0, fullFileName.length - ICalSource.FileExtension.length)

        val name = s"Schulferien.org / $countryName / $fileName"
        val id = s"schulferien.org-$countryName-$fileName"
        new LocalICalSource(name, id, path.toFile)
    }

    private def listContent(path: Path): Seq[Path] = Files.list(path).iterator().asScala.toSeq
    private def listFolders(path: Path): Seq[Path] = listContent(path).filter(Files.isDirectory(_))
    private def listFiles(path: Path, suffix: String): Seq[Path] = listContent(path)
        .filter(Files.isRegularFile(_))
        .filter(_.getFileName.toString.endsWith(suffix))

    def resolve(year: Int)(implicit ec: ExecutionContext): Future[Year] = {
        val relevantSources = sources(year)
        Future.sequence(relevantSources.map(_.getEvents(year, timeZone))).map(_.flatten.toVector).map { events =>
            val days = YearResolver.daysOfYear(year).map { date =>
                val weekOfYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                val dayOfWeek = date.getDayOfWeek.getValue
                Day(date, dayOfWeek, weekOfYear, events.filter(_.isWithin(date)))
            }

            val today = LocalDate.now()
            Year(year, today, today.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), days, relevantSources.map(s => Source(s.name, s.id)).toSet)
        }
    }
}

object YearResolver {
    def daysOfYear(year: Int): LazyList[LocalDate] = daysFrom(ofYearDay(year, 1)).takeWhile(_.getYear == year)

    def daysFrom(start: LocalDate): LazyList[LocalDate] = start #:: daysFrom(start).map(_.plus(1, DAYS))
}