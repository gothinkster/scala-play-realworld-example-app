package articles.services

import commons.utils.RealWorldStringUtils
import commons.validations.PropertyViolation
import commons.validations.constraints._
import articles.models.{ArticleUpdate, NewArticle}
import org.apache.commons.lang3.StringUtils

import scala.concurrent.ExecutionContext

class ArticleValidator(implicit private val ec: ExecutionContext) {

  private val titleValidator = new TitleValidator
  private val descriptionValidator = new DescriptionValidator
  private val bodyValidator = new BodyValidator
  private val tagValidator = new TagValidator

  def validateNewArticle(newArticle: NewArticle): Seq[PropertyViolation] = {
    require(newArticle != null)

    validateTitle(newArticle.title) ++
      validateDescription(newArticle.body) ++
      validateBody(newArticle.body) ++
      validateTags(newArticle.tagList)
  }

  private def validateTags(tags: Seq[String]) = tags.flatMap(tagValidator.validate)

  def validateArticleUpdate(articleUpdate: ArticleUpdate): Seq[PropertyViolation] = {
    val titleViolations = articleUpdate.title.map(validateTitle).getOrElse(Seq.empty)
    val descriptionViolations = articleUpdate.description.map(validateDescription).getOrElse(Seq.empty)
    val bodyViolations = articleUpdate.body.map(validateBody).getOrElse(Seq.empty)

    titleViolations ++ descriptionViolations ++ bodyViolations
  }

  private def validateTitle(title: String) = titleValidator.validate(title)

  private def validateDescription(description: String) = descriptionValidator.validate(description)

  private def validateBody(body: String) = bodyValidator.validate(body)

  private class StringValidator(minLength: Int = 0, maxLength: Int = Int.MaxValue) {

    def validate(str: String): Seq[Violation] = {
      if (StringUtils.isBlank(str)) Seq(NotNullViolation)
      else if (str.length < minLength) Seq(MinLengthViolation(minLength))
      else if (str.length > maxLength) Seq(MaxLengthViolation(maxLength))
      else if (RealWorldStringUtils.startsWithWhiteSpace(str)
        || RealWorldStringUtils.endsWithWhiteSpace(str)) Seq(PrefixOrSuffixWithWhiteSpacesViolation)
      else Nil
    }
  }

  private class TitleValidator {
    private val maxLength = 255
    private val stringValidator = new StringValidator(maxLength = maxLength)

    def validate(title: String): Seq[PropertyViolation] = {
      stringValidator.validate(title)
        .map(PropertyViolation("title", _))
    }
  }

  private class DescriptionValidator {
    private val maxLength = 255
    private val stringValidator = new StringValidator(maxLength = maxLength)

    def validate(description: String): Seq[PropertyViolation] = {
      stringValidator.validate(description)
        .map(PropertyViolation("description", _))
    }
  }

  private class BodyValidator {
    private val stringValidator = new StringValidator

    def validate(body: String): Seq[PropertyViolation] = {
      stringValidator.validate(body)
        .map(PropertyViolation("body", _))
    }
  }

  private class TagValidator {
    private val maxLength = 255
    private val stringValidator = new StringValidator(maxLength = maxLength)

    def validate(tag: String): Seq[PropertyViolation] = {
      stringValidator.validate(tag)
        .map(PropertyViolation("tag", _))
    }
  }

}