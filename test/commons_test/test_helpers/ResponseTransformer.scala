package commons_test.test_helpers

import play.api.libs.json.Reads
import play.api.libs.ws.WSResponse

trait ResponseTransformer[T] {
  def apply(rawResponse: WSResponse): T
}

object ResponseTransformer {
  implicit val identityResponseTransformer: ResponseTransformer[WSResponse] = IdentityResponseTransformer
}

object FromJsonToModelResponseTransformerFactory {
  def apply[WrapperType : Reads, ResultType](unwrap: WrapperType => ResultType): ResponseTransformer[ResultType] = {
    rawResponse: WSResponse => {
      unwrap(rawResponse.json.as[WrapperType])
    }
  }
}

object IdentityResponseTransformer extends ResponseTransformer[WSResponse] {
  override def apply(rawResponse: WSResponse): WSResponse = {
    rawResponse
  }
}
