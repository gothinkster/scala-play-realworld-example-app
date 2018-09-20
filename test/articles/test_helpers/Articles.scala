package articles.test_helpers

import articles.models.NewArticle

object Articles {
  val hotToTrainYourDragon: NewArticle = NewArticle("how-to-train-your-dragon", "Ever wonder how?",
    "It takes a Jacobian", Seq(Tags.dragons.name))
}
