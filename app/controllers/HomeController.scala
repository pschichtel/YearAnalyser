package controllers

import java.time.LocalDate

import controllers.Assets.Asset
import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import services.{SourcesExecutionContext, YearResolver}

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(analyser: YearResolver, assets: Assets, private implicit val ec: SourcesExecutionContext, cc: ControllerComponents) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index = Action {
    Ok(views.html.index())
  }

  def thisYear() = theYear(LocalDate.now().getYear)

  def theYear(year: Int) = Action.async {

    analyser.resolve(year) map { year =>
      Ok(Json.toJson(year))
    }
  }

  def getAsset(path: String, file: Asset) = assets.versioned(path, file)

}
