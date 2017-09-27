package config

import java.util.UUID

import _root_.controllers.AssetsComponents
import articles.ArticleComponents
import authentication.AuthenticationComponents
import play.api.ApplicationLoader.Context
import play.api._
import play.api.cache.AsyncCacheApi
import play.api.cache.ehcache.EhCacheComponents
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.db.slick._
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.i18n._
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc._
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import slick.basic.{BasicProfile, DatabaseConfig}
import users.UserComponents

class RealWorldApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = new RealWorldComponents(context).application
}

class RealWorldComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with SlickComponents
  with SlickEvolutionsComponents
  with AssetsComponents
  with I18nComponents
  with EvolutionsComponents
  with AhcWSComponents
  with AuthenticationComponents
  with UserComponents
  with ArticleComponents
  with EhCacheComponents {

  override lazy val slickApi: SlickApi =
    new DefaultSlickApi(environment, configuration, applicationLifecycle)(executionContext)

  override lazy val databaseConfigProvider: DatabaseConfigProvider = new DatabaseConfigProvider {
    def get[P <: BasicProfile]: DatabaseConfig[P] = slickApi.dbConfig[P](DbName("default"))
  }

  override lazy val dynamicEvolutions: DynamicEvolutions = new DynamicEvolutions

  def onStart(): Unit = {
    // applicationEvolutions is a val and requires evaluation
    applicationEvolutions
  }

  onStart()

  // set up logger
  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment, context.initialConfiguration, Map.empty)
  }

  protected lazy val routes: PartialFunction[RequestHeader, Handler] = userRoutes
    .orElse(authenticationRoutes)
    .orElse(articleRoutes)

  override lazy val router: Router = Router.from(routes)

  override lazy val defaultCacheApi: AsyncCacheApi = cacheApi(UUID.randomUUID().toString)

  override def httpFilters: Seq[EssentialFilter] = Nil
}