package config

import articles.controllers.mappings.ArticleJsonMappings
import play.api.libs.ws.{JsonBodyReadables, JsonBodyWritables}
import users.controllers.mappings.UserJsonMappings

trait JsonMappings extends JsonBodyReadables
  with JsonBodyWritables
  with ArticleJsonMappings
  with UserJsonMappings