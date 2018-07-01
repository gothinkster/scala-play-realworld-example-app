package articles.services

import articles.models.Tag
import articles.repositories.TagRepo
import slick.dbio.DBIO

class TagService(tagRepo: TagRepo) {

  def findAll: DBIO[Seq[Tag]] = {
    tagRepo.findAll
  }

}