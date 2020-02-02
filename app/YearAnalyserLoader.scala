import java.util.TimeZone

import controllers.{AssetsComponents, HomeController}
import play.api.cache.caffeine.CaffeineCacheComponents
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}
import play.filters.HttpFiltersComponents
import play.filters.csrf.CSRFComponents
import services.{SourcesExecutionContext, YearResolver}

class YearAnalyserLoader extends ApplicationLoader {
    override def load(context: ApplicationLoader.Context): Application = {
        LoggerConfigurator(context.environment.classLoader).foreach {
            _.configure(context.environment, context.initialConfiguration, Map.empty)
        }
        new YearAnalyserComponents(context).application
    }
}

class YearAnalyserComponents(context: ApplicationLoader.Context)
    extends BuiltInComponentsFromContext(context)
        with HttpFiltersComponents
        with AssetsComponents
        with CSRFComponents
        with CaffeineCacheComponents
        with AhcWSComponents {

    // Execution contexts
    private val sourcesExecutionContext = new SourcesExecutionContext(actorSystem)

    // Services
    private val yearResolverService = new YearResolver(TimeZone.getDefault, defaultCacheApi, wsClient, configuration)

    // Controllers
    private val homeController = new HomeController(yearResolverService, assets, sourcesExecutionContext, controllerComponents)

    // The router
    override def router: Router = new _root_.router.Routes(
        httpErrorHandler,
        homeController
    )
}