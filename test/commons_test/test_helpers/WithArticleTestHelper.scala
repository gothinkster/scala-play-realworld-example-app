package commons_test.test_helpers

import articles.models._
import articles.test_helpers.{ArticleTestHelper, CommentTestHelper}
import core.tags.test_helpers.TagTestHelper

trait WithArticleTestHelper {
  _: WithTestExecutionContext =>

  implicit val articleWrapperResponseTransformer: ResponseTransformer[ArticleWithTags] = {
    FromJsonToModelResponseTransformerFactory[ArticleWrapper, ArticleWithTags](_.article)
  }

  implicit val commentWithAuthorWrapperResponseTransformer: ResponseTransformer[CommentWithAuthor] = {
    FromJsonToModelResponseTransformerFactory[CommentWrapper, CommentWithAuthor](_.comment)
  }

  implicit val commentListWrapperResponseTransformer: ResponseTransformer[CommentList] = {
    FromJsonToModelResponseTransformerFactory[CommentList, CommentList](identity)
  }

  implicit val tagListWrapperResponseTransformer: ResponseTransformer[Seq[String]] = {
    FromJsonToModelResponseTransformerFactory[TagListWrapper, Seq[String]](_.tags)
  }

  def articleTestHelper: ArticleTestHelper = new ArticleTestHelper(executionContext)

  def commentTestHelper: CommentTestHelper = new CommentTestHelper(executionContext)

  def tagTestHelper: TagTestHelper = new TagTestHelper(executionContext)

}
