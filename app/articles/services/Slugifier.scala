package articles.services

import java.util.UUID

import com.github.slugify.Slugify

class Slugifier {

  private val slugifier = new Slugify()

  def slugify(title: String): String = {
    val slugifiedTitle = slugifier.slugify(title)
    val randomPart = UUID.randomUUID()
    s"$slugifiedTitle-$randomPart"
  }

}