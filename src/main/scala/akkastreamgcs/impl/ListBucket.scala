package akkastreamsgcs.impl

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{FormData, HttpRequest, Uri}
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akkastreamsgcs.{BucketListResponse, BucketObject, GoogleProtocols}
import spray.json._

case class ListBucketRequest(
  bucket: String,
  delimiter: Option[String],
  prefix: Option[String],
  token: String
)

object ListBucketRequest {
  def requestToFormData(
    request: ListBucketRequest,
    continuation_token: Option[String]
  ) : FormData = {
    val uri_builder = Query.newBuilder
    if(!request.delimiter.isEmpty)
      uri_builder += (("delimiter", request.delimiter.get))
    if(!request.prefix.isEmpty)
      uri_builder += (("prefix", request.prefix.get))
    if(!continuation_token.isEmpty)
      uri_builder += (("pageToken", continuation_token.get))
    FormData(uri_builder.result())
  }
}

object ListBucket extends GoogleAPI with GoogleProtocols {

  private def listBucketRequest(
    request: ListBucketRequest,
    continuation_token: Option[String]
  ) : HttpRequest = {
    HttpRequest(
      GET,
      uri = Uri.from(scheme = scheme, host = host, path = storageuri + request.bucket + "/o"),
      headers = List(Authorization(OAuth2BearerToken(request.token))),
      entity = ListBucketRequest.requestToFormData(request, continuation_token).toEntity
    )
  }

  /** list lists contents in a Google bucket */
  def list(
    request: ListBucketRequest
  ) (
    implicit system: ActorSystem, mat: ActorMaterializer
  ) : Source[BucketObject, akka.NotUsed] = {
    import mat.executionContext
    def listBucketCall(
      continuation_token: Option[String] = None
    ) : Future[BucketListResponse] =
      Http()
        .singleRequest(listBucketRequest(request, continuation_token))
        .flatMap(_.entity.dataBytes.runReduce((a, b) => a++b))
        .map(a => {
          a.utf8String.parseJson.convertTo[BucketListResponse]
        })

    def fileSourceFromFuture(
      f: Future[BucketListResponse]
    ): Source[BucketObject, akka.NotUsed] =
      Source
        .fromFuture(f)
        .flatMapConcat(res => {
          val keys = Source.fromIterator(() => res.items.getOrElse(Seq()).toIterator)
          if (!res.nextPageToken.isEmpty) {
            keys.concat(fileSourceFromFuture(listBucketCall(res.nextPageToken)))
          } else
            keys
        })
    fileSourceFromFuture(listBucketCall())
  }
}
