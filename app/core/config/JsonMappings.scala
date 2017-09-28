package core.config

import core.articles.controllers.mappings.ArticleJsonMappings
import play.api.libs.ws.{JsonBodyReadables, JsonBodyWritables}
import core.users.controllers.mappings.UserJsonMappings

trait JsonMappings extends JsonBodyReadables
  with JsonBodyWritables
  with ArticleJsonMappings
  with UserJsonMappings