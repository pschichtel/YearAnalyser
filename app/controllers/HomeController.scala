package controllers

import controllers.Assets.Asset
import javax.inject._
import play.api.cache.AsyncCacheApi
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc._
import services.event.{FerienApi, InThePast, Weekends}
import services.{SourcesExecutionContext, YearAnalyser}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(analyser: YearAnalyser, assets: Assets, private implicit val ec: SourcesExecutionContext, cache: AsyncCacheApi, client: WSClient, cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index())
  }

  def theYear = Action.async {
    val sources = Seq(Weekends, InThePast, new FerienApi(cache, client))

    analyser.getCurrentYear(sources) map { year =>
      Ok(Json.toJson(year))
    }
  }

  def getAsset(path: String, file: Asset) = assets.versioned(path, file)

}
