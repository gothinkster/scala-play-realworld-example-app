package core.articles.services

import core.articles.models.Tag
import core.articles.repositories.TagRepo
import slick.dbio.DBIO

class TagService(tagRepo: TagRepo) {

  def all: DBIO[Seq[Tag]] = {
    tagRepo.all
  }

}