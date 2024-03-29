package services

import org.apache.pekko.actor.ActorSystem

import javax.inject.{Inject, Singleton}
import play.api.libs.concurrent.CustomExecutionContext

@Singleton
class SourcesExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "contexts.sources")
