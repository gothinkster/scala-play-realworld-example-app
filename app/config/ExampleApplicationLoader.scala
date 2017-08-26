package config

import _root_.controllers.AssetsComponents
import articles.ArticleComponents
import authentication.AuthenticationsComponents
import be.objectify.deadbolt.scala.ActionBuilders
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.db.slick._
import play.api.db.slick.evolutions.SlickEvolutionsComponents
import play.api.i18n._
import play.api.inject.{Injector, SimpleInjector}
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.routing.Router
import play.filters.HttpFiltersComponents
import slick.basic.{BasicProfile, DatabaseConfig}
import users.UsersComponents

class ExampleApplicationLoader extends ApplicationLoader {
  def load(context: Context): Application = new ExampleComponents(context).application
}

class ExampleComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with SlickComponents
  with SlickEvolutionsComponents
  with AssetsComponents
  with I18nComponents
  with HttpFiltersComponents
  with EvolutionsComponents
  with AhcWSComponents
  with AuthenticationsComponents
  with UsersComponents
  with ArticleComponents {

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

  lazy val actionBuilders: ActionBuilders = createActionBuilders(playBodyParsers)

  lazy val router: Router = Router.from(userRoutes
    .orElse(authenticationRoutes)
    .orElse(articleRoutes)
  )

}