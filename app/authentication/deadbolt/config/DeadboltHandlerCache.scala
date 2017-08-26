package authentication.deadbolt.config

import javax.inject.{Inject, Singleton}

import be.objectify.deadbolt.scala.{DeadboltHandler, HandlerKey}
import be.objectify.deadbolt.scala.cache.HandlerCache

@Singleton
private[authentication] class DeadboltHandlerCache(defaultHandler: OAuthDeadboltHandler)
  extends HandlerCache {

  override def apply(): DeadboltHandler = defaultHandler

  override def apply(handlerKey: HandlerKey): DeadboltHandler = defaultHandler

}
